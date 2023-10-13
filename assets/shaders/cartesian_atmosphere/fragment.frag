/** Referenced from Sebastian Lague at https://www.youtube.com/wch?v=DxfEbulyFcY */
#ifdef GL_ES
precision mediump float;
#endif

#define WAVELENGTHS vec3(700, 530, 440)
#define SCATTER_DIVISOR 400.0
#define SCATTER_STRENGTH 0.112
#define RAYLEIGH_CONSTANTS vec3(pow(SCATTER_DIVISOR / WAVELENGTHS.r, 4.0), pow(SCATTER_DIVISOR / WAVELENGTHS.g, 4.0), pow(SCATTER_DIVISOR / WAVELENGTHS.b, 4.0)) * SCATTER_STRENGTH
#define DENSITY_FALLOFF 2.65
#define MAX_IN_SCATTER_POINTS 20
#define MAX_OPTICAL_DEPTH_POINTS 20
#define INTENSITY 0.988
#define EPSILON 1e-4
#define C_PI 3.1415926535897932384626433832795

struct Celestial {
    vec2 pos;
    float radius;
};

varying vec2 v_texcoord;
varying vec2 v_worldcoord;

uniform sampler2D u_texture;

uniform float u_planetRadius;
uniform float u_planetCircumference;
uniform float u_atmosRadius;
uniform vec4 u_atmosColor;
uniform vec3 u_starDirection;

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

    return vec2(1.0 / 0.0, 0.0);
}

vec3 cartesianOriginToPolar(vec3 rayOrigin){
    float theta = (rayOrigin.x / u_planetCircumference) * 2.0 * C_PI;
    float polarX = cos(theta) * rayOrigin.y;
    float polarY = sin(theta) * rayOrigin.y;
    return vec3(polarX, polarY, rayOrigin.z);
}

float densityAtPoint(vec3 sample){
    float heightAboveSurface = length(sample) - u_planetRadius;
    float height01 = heightAboveSurface / (u_atmosRadius - u_planetRadius);
    float density = exp(-height01 * DENSITY_FALLOFF) * (1.0 - height01);
    return density;
}

float opticalDepth(vec3 rayOrigin, vec3 rayDir, float rayLength){
    vec3 sample = rayOrigin;
    float stepSize = rayLength / float(MAX_OPTICAL_DEPTH_POINTS - 1);
    float opticalDepth = 0.0;

    for(int i = 0; i < MAX_OPTICAL_DEPTH_POINTS; i++){
        float localDensity = densityAtPoint(sample);
        opticalDepth += localDensity;
        sample += rayDir * stepSize;
    }
    opticalDepth *= stepSize;

    return opticalDepth;
}

vec3 light(vec3 rayOrigin, vec3 rayDir, float rayLength){
    vec3 inScatterLight = vec3(0.0);
    vec3 inScatterPoint = rayOrigin;
    float stepSize = rayLength / float(MAX_IN_SCATTER_POINTS - 1);

    for(int i = 0; i < MAX_IN_SCATTER_POINTS; i++){
        float sunRayLength = sphereRaycast(vec3(0.0), u_atmosRadius, inScatterPoint, u_starDirection).y;
        float sunRayOpticalDepth = opticalDepth(inScatterPoint, u_starDirection, sunRayLength);
        float viewRayOpticalDepth = opticalDepth(inScatterPoint, -rayDir, stepSize * float(i));
        float localDensity = densityAtPoint(inScatterPoint);
        vec3 transmittance = exp(-(sunRayOpticalDepth + viewRayOpticalDepth) * RAYLEIGH_CONSTANTS);

        inScatterLight += localDensity * transmittance;
        inScatterPoint += rayDir * stepSize;
    }
    inScatterLight *= RAYLEIGH_CONSTANTS * INTENSITY * stepSize;

    return inScatterLight;
}

void main() {
    // vec4 color = texture2D(u_texture, v_texcoord);
    vec4 color = vec4(0.0);

    vec3 rayOrigin = cartesianOriginToPolar(vec3(v_worldcoord, u_atmosRadius * -2.0));
    vec3 rayDir = vec3(0, 0, 1);

    vec2 surfaceRay = sphereRaycast(vec3(0.0), u_planetRadius, rayOrigin, rayDir);
    vec2 atmosRay = sphereRaycast(vec3(0.0), u_atmosRadius, rayOrigin, rayDir);

    float distToAtmos = atmosRay.x;
    float distThruAtmos = min(atmosRay.y, surfaceRay.x - atmosRay.x);

    if(distThruAtmos > 0.0){
        vec3 pointInAtmosphere = rayOrigin + rayDir * (distToAtmos + EPSILON);
        vec3 light = light(pointInAtmosphere, rayDir, distThruAtmos - EPSILON * 2.0);
        color = vec4((color.rgb * u_atmosColor.rgb) * (1.0 * light) + light, 1.0);
        color = vec4(color.rgb, length(color.rgb));
    }

    gl_FragColor = color;
}