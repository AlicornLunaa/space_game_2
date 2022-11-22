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

#define CAMERA_POS vec3(0, 0, 10.0)
#define SUN_POS vec3(50.0, 0.5, 50.0)
#define PLANET_POS vec3(0.5, 0.5, 0.0)
#define PLANET_RAD 0.2
#define ATMOS_RAD 0.5

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform vec2 u_resolution;
uniform vec2 u_mouse;
uniform vec3 u_camera;
uniform float u_time;

float circleShape(vec2 position, float radius){
    return 1.0 - step(radius, length(position));
}

vec3 fakeSphere(vec2 center, float radius, vec2 position){
    vec2 p = position - center;
    vec3 threeD = vec3(position.x, position.y, sqrt(pow(radius, 2.0) - pow(p.x, 2.0) - pow(p.y, 2.0)));
    
    if(length(p) > radius) return vec3(position, 0.0);
    return threeD;
}

vec3 intersectCircle(vec2 target, vec2 origin, vec2 sphereCenter, float r) {
    vec2 d = target - origin;

    float a = dot(d, d);
    float b = 2.0 * dot(d, origin - sphereCenter);
    float c = dot(sphereCenter, sphereCenter) + dot(origin, origin) - 2.0 * dot(sphereCenter, origin) - r*r;
    float test = b*b - 4.0*a*c;

    bool behind = length(origin - target) < length(origin - sphereCenter);
    
	if (test >= 0.0 && !behind) {
  		float u = (-b - sqrt(test)) / (2.0 * a);
  	    vec2 hitp = origin + u * (target - origin);
        
        if (length(hitp.xy - target) > length(target - origin)) {
            return vec3(-1.0,0.0,.0); 
        }
        
        return vec3(1.0, hitp);
    }

    return vec3(-1.0,0.0,.0);   
}

vec2 circleRaycast(vec2 center, float radius, vec2 rayOrigin, vec2 rayDir){
    vec2 offset = rayOrigin - center;
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
    float distFromSurface = (length(sample - PLANET_POS) - PLANET_RAD) / (ATMOS_RAD - PLANET_RAD);
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
        float sunRayLength = sphereRaycast(PLANET_POS, ATMOS_RAD, inScatterPoint, dirToSun).y;
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
    vec2 fragPos = (gl_FragCoord.xy / u_resolution);
    vec3 fragPos3D = fakeSphere(PLANET_POS.xy, PLANET_RAD, fragPos);
    vec3 fragToSun = normalize(SUN_POS - fragPos3D);
    vec3 fragToCam = normalize(fragPos3D - CAMERA_POS);

    // if(distance(fragPos3D, PLANET_POS) - 0.0000001 <= PLANET_RAD) return vec4(0, 0, 0, 1);
    
    vec2 hitInfo = sphereRaycast(PLANET_POS, ATMOS_RAD, CAMERA_POS, fragToCam);
    float distToSurface = sphereRaycast(PLANET_POS, PLANET_RAD, CAMERA_POS, fragToCam).y;
    float distToAtmos = hitInfo.x;
    float distThruAtmos = hitInfo.y;

    if(distThruAtmos > 0.0){
        vec3 light = calculateLight(fragPos3D, fragToCam, distThruAtmos, fragToSun, v_color.rgb);
        return vec4(light, 1.0);
    }

    return vec4(0, 0, 0, 1);
}

void main(){
    vec2 fragPos = (gl_FragCoord.xy / u_resolution);
    float terrainColor = circleShape(fragPos - PLANET_POS.xy, PLANET_RAD);

    // gl_FragColor = calculateScattering();
    gl_FragColor = vec4(0.4, 0.8, 0.3, 1.0) * terrainColor + calculateScattering();
}