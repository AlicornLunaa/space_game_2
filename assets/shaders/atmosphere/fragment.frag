#ifdef GL_ES
precision mediump float;
#endif

#define PI 3.14159265359
#define MAX_FLOAT 34000000000000000000.0

#define RED_WAVELENGTH 700.0
#define GREEN_WAVELENGTH 530.0
#define BLUE_WAVELENGTH 440.0
#define SCATTER_DIVISOR 400.0
#define SCATTER_STRENGTH 0.4
#define DENSITY_FALLOFF 1.0

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

float atmosDensityAtPoint(vec2 planetCenter, float planetRadius, float atmosRadius, vec2 pos){
    // 1 at sea level, decrease exponentially
    float distFromSurface = (length(pos - planetCenter) - planetRadius) / (atmosRadius - planetRadius);
    return exp(-distFromSurface * DENSITY_FALLOFF) * (1.0 - distFromSurface);
}

float rayleigh(float scatter, float angle, float density){
    return (pow(PI, 2.0) / 2.0) * density * scatter * (1.0 + pow(cos(angle), 2.0));
}

vec3 calculateScattering(vec2 center, float planetRadius, float atmosRadius, vec2 fragPos, vec2 sunPos){
    vec3 fragToSun = normalize(vec3(fragPos, 0.0) - vec3(sunPos, 0.0));
    vec3 fragToCam = normalize(vec3(fragPos, 0.0) - u_camera);
    float angle = acos(dot(fragToSun, fragToCam));

    vec2 hitInfo = circleRaycast(center, atmosRadius, fragPos, fragToSun.xy);
    float distInAtmos = hitInfo.y;

    float scatterR = pow(SCATTER_DIVISOR / RED_WAVELENGTH, 4.0) * SCATTER_STRENGTH;
    float scatterG = pow(SCATTER_DIVISOR / GREEN_WAVELENGTH, 4.0) * SCATTER_STRENGTH;
    float scatterB = pow(SCATTER_DIVISOR / BLUE_WAVELENGTH, 4.0) * SCATTER_STRENGTH;

    float density = atmosDensityAtPoint(center, planetRadius, atmosRadius, fragPos);
    float redVal = rayleigh(scatterR, angle, density);
    float greenVal = rayleigh(scatterG, angle, density);
    float blueVal = rayleigh(scatterB, angle, density);

    return (density >= 1.0) ? vec3(0.0) : vec3(redVal, greenVal, blueVal);
}

void main(){
    vec2 position = (gl_FragCoord.xy / u_resolution);
    vec2 localMouse = u_mouse / u_resolution;

    vec2 sunPos = vec2(50.0, 0.0);
    vec2 center = vec2(0.5);
    float planetRadius = 0.2;
    float atmosRadius = 0.3;

    float terrainColor = circleShape(position - center, planetRadius);

    vec3 atmosphereColor = calculateScattering(center, planetRadius, atmosRadius, position, sunPos);
    atmosphereColor *= (intersectCircle(sunPos, position, center, planetRadius).x >= 0.0) ? 0.0 : 1.0;

    gl_FragColor = vec4(0, 1, 0, 1.00) * terrainColor + vec4(atmosphereColor, 1);
}