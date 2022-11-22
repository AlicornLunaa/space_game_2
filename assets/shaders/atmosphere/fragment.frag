/** Reference Sebastian Lague https://www.youtube.com/watch?v=DxfEbulyFcY */
#ifdef GL_ES
precision mediump float;
#endif

#define PI 3.14159265359
#define MAX_FLOAT 34000000000000000000.0

#define WAVELENGTHS vec3(700, 530, 440)
#define SCATTER_DIVISOR 400.0
#define SCATTER_STRENGTH 5.5
#define RAYLEIGH_CONSTANTS vec3(pow(SCATTER_DIVISOR / WAVELENGTHS.r, 4.0), pow(SCATTER_DIVISOR / WAVELENGTHS.g, 4.0), pow(SCATTER_DIVISOR / WAVELENGTHS.b, 4.0)) * SCATTER_STRENGTH
#define DENSITY_FALLOFF 15.0
#define IN_SCATTER_POINTS 10
#define OPTICAL_DEPTH_POINTS 10

#define planetPosition vec3(0.5, 0.5, 0.0)
#define cameraWorldPos vec3(0.0, 0.0, -50.0)

varying vec2 v_texcoord;

uniform sampler2D u_texture;
uniform vec3 atmosColor;
uniform vec3 starDirection;
uniform float atmosPlanetRatio;

vec3 fakeSphere(vec2 center, float radius, vec2 position){
    vec2 p = position - center;
    vec3 threeD = vec3(position.x, position.y, sqrt(pow(radius, 2.0) - pow(p.x, 2.0) - pow(p.y, 2.0)));
    
    if(length(p) > radius) return vec3(position, 0.0);
    return threeD;
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
    float distFromSurface = (length(sample - planetPosition) - atmosPlanetRatio) / (1.0 - atmosPlanetRatio);
    return exp(-distFromSurface * DENSITY_FALLOFF) * (1.0 - distFromSurface);
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
        float sunRayLength = sphereRaycast(planetPosition, 0.5, inScatterPoint, dirToSun).y;
        float sunRayOpticalDepth = opticalDepth(inScatterPoint, dirToSun, sunRayLength);
        vec3 transmittance = exp(-(sunRayOpticalDepth + viewRayOpticalDepth) * RAYLEIGH_CONSTANTS);
        float localDensity = densityAtPoint(inScatterPoint);
        viewRayOpticalDepth = opticalDepth(inScatterPoint, -rayDir, stepSize * float(i));

        inScatterLight += localDensity * transmittance * RAYLEIGH_CONSTANTS;
        inScatterPoint += rayDir * stepSize;
    }

    return inScatterLight;
}

vec4 calculateScattering(){
    vec3 fragPos3D = fakeSphere(planetPosition.xy, 0.5, v_texcoord);
    vec3 fragToSun = normalize(starDirection);
    vec3 fragToCam = normalize(fragPos3D - cameraWorldPos);

    if(distance(fragPos3D, planetPosition) - 0.0000001 <= (atmosPlanetRatio / 2.0)) return vec4(0, 0, 0, 0);
    
    vec2 hitInfo = sphereRaycast(planetPosition, 0.5, cameraWorldPos, fragToCam);
    float distToAtmos = hitInfo.x;
    float distThruAtmos = hitInfo.y;

    if(distThruAtmos > 0.0){
        vec3 light = calculateLight(fragPos3D, fragToCam, distThruAtmos, fragToSun, atmosColor);
        return vec4(light, length(light));
    }

    return vec4(0, 0, 0, 0);
}

void main(){
    gl_FragColor = calculateScattering();
}