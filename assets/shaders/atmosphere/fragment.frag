/** Referenced from Sebastian Lague at https://www.youtube.com/watch?v=DxfEbulyFcY */
#ifdef GL_ES
precision mediump float;
#endif

#define WAVELENGTHS vec3(700, 530, 440)
#define SCATTER_DIVISOR 400.0
#define SCATTER_STRENGTH 2.0
#define RAYLEIGH_CONSTANTS vec3(pow(SCATTER_DIVISOR / WAVELENGTHS.r, 4.0), pow(SCATTER_DIVISOR / WAVELENGTHS.g, 4.0), pow(SCATTER_DIVISOR / WAVELENGTHS.b, 4.0)) * SCATTER_STRENGTH
#define DENSITY_FALLOFF 8.0
#define MAX_IN_SCATTER_POINTS 8
#define MAX_OPTICAL_DEPTH_POINTS 8
#define EPSILON 1e-4

#define u_planetCenter vec3(0.0, 0.0, 2.235)
#define u_atmosRadius 1.5

struct Celestial {
    vec2 pos;
    float radius;
};

varying vec2 v_texcoord;

uniform sampler2D u_texture;
uniform vec4 u_atmosColor;
uniform vec3 u_starDirection;
uniform float u_planetRadius;
uniform Celestial u_occluder;
uniform float u_occlusionEnabled;

vec2 sphereRaycast(vec3 center, float radius, vec3 rayOrigin, vec3 rayDir){
    vec3 offset = rayOrigin - center;
    float a = 1.0;
    float b = 2.0 * dot(offset, rayDir);
    float c = dot(offset, offset) - radius * radius;
    float d = b * b - 4.0 * a * c;

    if(d > 0.0){
        float s = sqrt(d);
        float near = max(0.0, (-b - s) / (2.0 * a));
        float far = (-b + s) / (2.0 * a);

        if(far >= 0.0){
            return vec2(near, far - near);
        }
    }

    return vec2(0.0);
}

float densityAtPoint(vec3 sample){
    float heightAboveSurface = length(sample - u_planetCenter) - u_planetRadius;
    float height01 = heightAboveSurface / (u_atmosRadius - u_planetRadius);
    float density = exp(-height01 * DENSITY_FALLOFF) * (1.0 - height01);
    return density;
}

float opticalDepth(vec3 rayOrigin, vec3 rayDir, float rayLength){
    vec3 sample = rayOrigin;
    float stepSize = rayLength / float(MAX_OPTICAL_DEPTH_POINTS - 1);
    float opticalDepth;

    for(int i = 0; i < MAX_OPTICAL_DEPTH_POINTS; i++){
        float localDensity = densityAtPoint(sample);
        opticalDepth += localDensity * stepSize;
        sample += rayDir * stepSize;
    }

    return opticalDepth;
}

vec3 light(vec3 rayOrigin, vec3 rayDir, float rayLength){
    vec3 inScatterLight = vec3(0.0);
    vec3 inScatterPoint = rayOrigin;
    float stepSize = rayLength / float(MAX_IN_SCATTER_POINTS - 1);

    for(int i = 0; i < MAX_IN_SCATTER_POINTS; i++){
        float sunRayLength = sphereRaycast(u_planetCenter, u_atmosRadius, inScatterPoint, u_starDirection).y;
        float sunRayOpticalDepth = opticalDepth(inScatterPoint, u_starDirection, sunRayLength);
        float viewRayOpticalDepth = opticalDepth(inScatterPoint, -rayDir, stepSize * float(i));
        float localDensity = densityAtPoint(inScatterPoint);
        vec3 transmittance = exp(-(sunRayOpticalDepth + viewRayOpticalDepth) * RAYLEIGH_CONSTANTS);

        inScatterLight += localDensity * transmittance * RAYLEIGH_CONSTANTS;
        inScatterPoint += rayDir * stepSize;
    }

    return u_atmosColor.rgb * inScatterLight;
}

float shadowCast(vec3 rayOrigin, vec3 rayDir){
    vec3 pos = vec3(u_occluder.pos, 0.0);
    float rad = u_occluder.radius;
    vec2 rayToStar = sphereRaycast(pos, rad, rayOrigin, rayDir);
    return (rayToStar.x > 0.0 && u_occlusionEnabled > 0.5) ? 1.0 : 0.0;
}

void main() {
    vec2 uv = (v_texcoord * 2.0 - 1.0);
    vec3 cameraZ = vec3(0, 0, 1);
    vec3 cameraX = vec3(1, 0, 0);
    vec3 cameraY = vec3(0, 1, 0);

    vec3 color = vec3(0, 0, 0);
    vec3 rayOrigin = vec3(0, 0, 0);
    vec3 rayDir = normalize(uv.x * cameraX + uv.y * cameraY + 2.0 * cameraZ);

    vec2 surfaceRay = sphereRaycast(u_planetCenter, u_planetRadius, rayOrigin, rayDir);
    vec2 atmosRay = sphereRaycast(u_planetCenter, u_atmosRadius, rayOrigin, rayDir);

    // Surface shading
    float tMin = 1e19;
    if(surfaceRay.x > 0.0 && tMin > surfaceRay.x) {
        vec3 sN = normalize((normalize(rayOrigin + rayDir * surfaceRay.x) - u_planetCenter));
        tMin = surfaceRay.x;
        color = vec3(max(0.0, dot(sN, u_starDirection)));
    }

    // Atmosphere shading
    float distToAtmos = atmosRay.x;
    float distThruAtmos = min(atmosRay.y, tMin - atmosRay.x);
    if(distThruAtmos > 0.0 && tMin > atmosRay.x) {
        vec3 pointInAtmosphere = rayOrigin + rayDir * (distToAtmos - EPSILON);
        vec3 light = light(pointInAtmosphere, rayDir, distThruAtmos + EPSILON * 2.0);
        color = color * (1.0 - light) + light;
    }

    // Shadow shading
    float inShadow = shadowCast(vec3(uv, 0.0), u_starDirection);

    gl_FragColor = vec4(color, length(color)) * (1.0 - inShadow);
}