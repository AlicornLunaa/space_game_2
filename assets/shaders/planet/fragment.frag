#ifdef GL_ES
precision mediump float;
#endif

#define EPSILON 1e-4
#define u_planetCenter vec3(0.0, 0.0, 2.235)
#define u_planetRadius 1.0

struct Celestial {
    vec2 pos;
    float radius;
};

varying vec2 v_texcoord;

uniform sampler2D u_texture;
uniform vec4 u_planetColor;
uniform vec3 u_starDirection;
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

    vec4 color = vec4(0, 0, 0, 0);
    vec3 rayOrigin = vec3(0, 0, 0);
    vec3 rayDir = normalize(uv.x * cameraX + uv.y * cameraY + 2.0 * cameraZ);

    vec2 surfaceRay = sphereRaycast(u_planetCenter, u_planetRadius, rayOrigin, rayDir);
    float distToSurface = surfaceRay.x;
    float distThruSurface = surfaceRay.y;

    float tMin = 1e19;
    if(distThruSurface > 0.0 && tMin > distToSurface) {
        vec3 sN = normalize((normalize(rayOrigin + rayDir * distToSurface) - u_planetCenter));
        tMin = surfaceRay.x;
        color = u_planetColor.rgba * vec4(vec3(max(0.0, dot(sN, u_starDirection))), 1.0);
    }

    float inShadow = shadowCast(vec3(uv, 0.0), u_starDirection);

    gl_FragColor = color * vec4(vec3(1.0 - inShadow), 1.0);
}