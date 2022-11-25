/** Reference Sebastian Lague https://www.youtube.com/watch?v=DxfEbulyFcY */
#ifdef GL_ES
precision mediump float;
#endif

#define PI 3.14159265359

#define WAVELENGTHS vec3(700, 530, 440)
#define SCATTER_DIVISOR 400.0
#define SCATTER_STRENGTH 1.0
#define RAYLEIGH_CONSTANTS vec3(pow(SCATTER_DIVISOR / WAVELENGTHS.r, 4.0), pow(SCATTER_DIVISOR / WAVELENGTHS.g, 4.0), pow(SCATTER_DIVISOR / WAVELENGTHS.b, 4.0)) * SCATTER_STRENGTH
#define DENSITY_FALLOFF 1.0
#define MAX_IN_SCATTER_POINTS 8
#define MAX_OPTICAL_DEPTH_POINTS 8
#define EPSILON 1e-4

#define u_starDirection vec3(1.0, 0.0, 0.0)
#define u_planetCenter vec3(0.0, 0.0, 0.0)
#define u_planetRadius 1.0
#define u_atmosRadius 2.0

#define sunPos normalize(vec3(cos(u_time), 0, sin(-u_time)))

varying vec2 v_texcoord;
varying vec3 v_viewvector;

uniform sampler2D u_texture;
uniform vec3 u_cameraPosition;
uniform float u_time;

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
            return vec2(far, far - near);
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
    float opticalDepth = 0.0;

    for(int i = 0; i < MAX_OPTICAL_DEPTH_POINTS; i++){
        float localDensity = densityAtPoint(sample);
        opticalDepth += localDensity * stepSize;
        sample += rayDir * stepSize;
    }

    return opticalDepth;
}

vec3 calculateLight(vec3 rayOrigin, vec3 rayDir, float rayLength){
    vec3 inScatterPoint = rayOrigin;
    float inScatterLight = 0.0;
    float stepSize = rayLength / float(MAX_IN_SCATTER_POINTS - 1);

    for(int i = 0; i < MAX_IN_SCATTER_POINTS; i++){
        float sunRayLength = sphereRaycast(u_planetCenter, u_atmosRadius, inScatterPoint, sunPos).y;
        float sunRayOpticalDepth = opticalDepth(inScatterPoint, sunPos, sunRayLength);
        float viewRayOpticalDepth = opticalDepth(inScatterPoint, -rayDir, stepSize * float(i));
        float transmittance = exp(-(sunRayOpticalDepth + viewRayOpticalDepth));
        float localDensity = densityAtPoint(inScatterPoint);

        inScatterLight += localDensity * transmittance * stepSize;
        inScatterPoint += rayDir * stepSize;
    }

    return vec3(inScatterLight);
}

vec4 calculateScattering(){
    vec3 color = vec3(0, 1, 0);
    vec3 rayOrigin = u_cameraPosition;
    vec3 rayDir = normalize(v_viewvector);

    float distToSurface = sphereRaycast(u_planetCenter, u_planetRadius, rayOrigin, rayDir).x;
    
    vec2 hitInfo = sphereRaycast(u_planetCenter, u_atmosRadius, rayOrigin, rayDir);
    float distToAtmos = hitInfo.x;
    float distThruAtmos = min(hitInfo.y, distToSurface - distToAtmos);
    
    if(distThruAtmos > 0.0){
        float epsilon = 0.0001;
        vec3 pointInAtmosphere = rayOrigin + rayDir * (distToAtmos + epsilon);
        vec3 light = calculateLight(pointInAtmosphere, rayDir, (distThruAtmos - epsilon * 2.0));
        return vec4(light, 1.0);
    }

    return vec4(color, 0.0);
}

void main(){
    gl_FragColor = calculateScattering();
}