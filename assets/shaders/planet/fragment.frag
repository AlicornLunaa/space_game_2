#ifdef GL_ES
precision mediump float;
#endif

#define EPSILON 1e-4

struct Celestial {
    vec2 pos;
    float radius;
};

varying vec2 v_texcoord;
varying vec2 v_worldcoord;

uniform sampler2D u_texture;

uniform vec3 u_starDirection;
uniform vec2 u_planetWorldPos;
uniform float u_planetRadius;

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

void main() {
    vec4 color = texture2D(u_texture, v_texcoord);

    vec3 rayOrigin = vec3(v_worldcoord, u_planetRadius * -2.0);
    vec3 rayDir = vec3(0, 0, 1);
    vec2 surfaceRay = sphereRaycast(vec3(u_planetWorldPos, 0.0), u_planetRadius, rayOrigin, rayDir);

    color *= vec4(vec3(surfaceRay.y / (u_planetRadius * 2.0)), 1.0);

    if(distance(v_worldcoord, u_planetWorldPos) > u_planetRadius){
        color *= 0.0;
    }

    rayOrigin = rayOrigin + rayDir * (surfaceRay.x - EPSILON);
    rayDir = u_starDirection;
    vec2 lightingRay = sphereRaycast(vec3(u_planetWorldPos, 0.0), u_planetRadius, rayOrigin, rayDir);

    if(lightingRay.y > 0.0){
        color *= 1.0 - (lightingRay.y / (u_planetRadius * 2.0));
    }

    gl_FragColor = color;
}