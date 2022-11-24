/** Reference Sebastian Lague https://www.youtube.com/watch?v=DxfEbulyFcY */
#ifdef GL_ES
precision mediump float;
#endif

#define PI 3.14159265359
#define MAX_FLOAT 34000000000000000000.0

#define WAVELENGTHS vec3(700, 530, 440)
#define SCATTER_DIVISOR 400.0
#define SCATTER_STRENGTH 8.0
#define RAYLEIGH_CONSTANTS vec3(pow(SCATTER_DIVISOR / WAVELENGTHS.r, 4.0), pow(SCATTER_DIVISOR / WAVELENGTHS.g, 4.0), pow(SCATTER_DIVISOR / WAVELENGTHS.b, 4.0)) * SCATTER_STRENGTH
#define DENSITY_FALLOFF 0.8
#define IN_SCATTER_POINTS 5
#define OPTICAL_DEPTH_POINTS 5

#define starDirection vec3(0.5, 0.0, 0.0)
#define planetPosition vec3(0.5, 0.5, 0.5)
#define cameraWorldPos vec3(0.5, 0.5, 50.0)
#define rad 0.5

varying vec2 v_texcoord;
varying vec3 v_viewvector;

uniform sampler2D u_texture;
uniform vec3 atmosColor;
uniform float atmosPlanetRatio;

float sphereDepth(vec2 sample, float radius){
    if(length(sample - planetPosition.xy) >= radius) return -0.5;
    return sqrt(pow(radius, 2.0) - pow(sample.x - planetPosition.x, 2.0) - pow(sample.y - planetPosition.y, 2.0)) + planetPosition.z;
}

vec2 sphereRaycast(vec3 center, float radius, vec3 rayOrigin, vec3 rayDir){
    vec3 offset = rayOrigin - center;
    float a = 1.0;
    float b = 2.0 * dot(offset, rayDir);
    float c = dot(offset, offset) - pow(radius, 2.0);
    float d = b * b - 4.0 * a * c;

    if(d > 0.0){
        float s = sqrt(d);
        float near = max(0.0, (-b - s) / (2.0 * a));
        float far = (-b + s) / (2.0 * a);

        if(far >= 0.0){
            return vec2(far, far - near);
        }
    }

    return vec2(MAX_FLOAT, 0.0);
}

float densityAtPoint(vec3 sample){
    float heightAboveSurface = length(sample - planetPosition) - (atmosPlanetRatio / 2.0);
    float height01 = heightAboveSurface / (rad - (atmosPlanetRatio / 2.0));
    float density = exp(-height01 * DENSITY_FALLOFF) * (1.0 - height01);
    return density;
}

float opticalDepth(vec3 rayOrigin, vec3 rayDir, float rayLength){
    vec3 sample = rayOrigin;
    float stepSize = rayLength / float(OPTICAL_DEPTH_POINTS - 1);
    float opticalDepth = 0.0;

    for(int i = 0; i < OPTICAL_DEPTH_POINTS; i++){
        float localDensity = densityAtPoint(sample);
        opticalDepth += localDensity * stepSize;
        sample += rayDir * stepSize;
    }

    return opticalDepth;
}

vec3 calculateLight(vec3 rayOrigin, vec3 rayDir, float rayLength, vec3 dirToSun, vec3 originalColor){
    vec3 inScatterPoint = rayOrigin;
    vec3 inScatterLight = vec3(0);
    float viewRayOpticalDepth = 0.0;
    float stepSize = rayLength / float(IN_SCATTER_POINTS - 1);

    for(int i = 0; i < IN_SCATTER_POINTS; i++){
        vec3 inScatterPointAdjusted = vec3(inScatterPoint.xy, inScatterPoint.z + 0.5);

        float sunRayLength = sphereRaycast(planetPosition, rad, inScatterPointAdjusted, dirToSun).y;
        float sunRayOpticalDepth = opticalDepth(inScatterPointAdjusted, dirToSun, sunRayLength);
        vec3 transmittance = exp(-(sunRayOpticalDepth + viewRayOpticalDepth) * RAYLEIGH_CONSTANTS);
        float localDensity = densityAtPoint(inScatterPointAdjusted);
        viewRayOpticalDepth = opticalDepth(inScatterPointAdjusted, -rayDir, stepSize * float(i));

        inScatterLight += localDensity * transmittance * RAYLEIGH_CONSTANTS;
        inScatterPoint += rayDir * stepSize;
    }

    return inScatterLight;
}

vec4 calculateScattering(){
    vec3 color = vec3(0, 0, 0);
    float depth = sphereDepth(v_texcoord, atmosPlanetRatio / 2.0);

    vec3 rayOrigin = cameraWorldPos;
    vec3 rayDir = normalize(vec3(v_texcoord, 0.0) - cameraWorldPos);
    vec3 fragToSun = normalize(starDirection);

    float distToSurface = sphereRaycast(planetPosition, atmosPlanetRatio / 2.145, rayOrigin, rayDir).x;
    
    vec2 hitInfo = sphereRaycast(planetPosition, rad, rayOrigin, rayDir);
    float distToAtmos = hitInfo.x;
    float distThruAtmos = min(hitInfo.y, distToSurface - distToAtmos);

    if(distance(v_texcoord, planetPosition.xy) <= atmosPlanetRatio / 2.14){
        color += vec3(0.1, 0.8, 0.2) * depth * 1.3;
    }

    if(distThruAtmos > 0.0){
        vec3 pointInAtmosphere = rayOrigin + rayDir * distToAtmos;
        vec3 light = calculateLight(pointInAtmosphere, rayDir, distThruAtmos, fragToSun, atmosColor);
        color += light * 0.2;
    }

    return vec4(color, length(color));
}

void main(){
    gl_FragColor = calculateScattering();
}