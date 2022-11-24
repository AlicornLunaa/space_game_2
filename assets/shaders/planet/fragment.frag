#ifdef GL_ES
precision mediump float;
#endif

#define PI 3.14159265359
#define MAX_FLOAT 34000000000000000000.0
#define CENTER vec3(0.5, 0.5, 0.5)
#define RADIUS 0.5

varying vec2 v_texcoord;

uniform sampler2D u_texture;
uniform vec3 starDirection;

vec2 sphereRaycast(vec3 rayOrigin, vec3 rayDir){
    vec3 offset = rayOrigin - CENTER;
    float a = 1.0;
    float b = 2.0 * dot(offset, rayDir);
    float c = dot(offset, offset) - pow(RADIUS, 2.0);
    float d = b * b - 4.0 * a * c;

    if(d > 0.0){
        float s = sqrt(d);
        float near = max(0.0, (-b - s) / (2.0 * a));
        float far = (-b + s) / (2.0 * a);

        if(far >= 0.0){
            return vec2(far, min(-b - s, -b + s));
        }
    }

    return vec2(MAX_FLOAT, 0.0);
}

float sphereDepth(vec2 sample){
    if(length(sample - CENTER.xy) >= RADIUS) return 0.0;
    return sqrt(pow(RADIUS, 2.0) - pow(sample.x - CENTER.x, 2.0) - pow(sample.y - CENTER.y, 2.0)) + CENTER.z;
}

void main(){
    vec3 position = vec3(v_texcoord, sphereDepth(v_texcoord));
    vec3 rayOrigin = vec3(-1.0, 0.5, 1.0);
    vec3 rayDir = normalize(position - rayOrigin);
    vec2 hitInfo = sphereRaycast(rayOrigin, rayDir);

    gl_FragColor = vec4(vec3(hitInfo.y), 1);
}