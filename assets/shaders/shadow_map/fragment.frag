#ifdef GL_ES
precision mediump float;
#endif

#define PI 3.141592654
#define THRESHOLD 0.75

varying vec2 v_texcoord;

uniform sampler2D u_texture;
uniform vec2 u_resolution;

void main(){
    float minDist = 1.0;

    for(float y = 0.0; y < u_resolution.y; y+= 1.0){
        // Cartesian to polar transform
        vec2 norm = vec2(v_texcoord.x, y / u_resolution.y) * 2.0 - 1.0;
        float theta = PI * 1.5 + norm.x * PI;
        float r = (1.0 + norm.y) * 0.5;

        // Sampling
        vec2 coord = vec2(-r * sin(theta), -r * cos(theta)) / 2.0 + 0.5;
        vec4 data = texture2D(u_texture, coord);
        float dist = y / u_resolution.y;

        // Save nearest
        float caster = length(data.rgb);
        if(caster > THRESHOLD){
            minDist = min(minDist, dist);
            break;
        }
    }

    gl_FragColor = vec4(vec3(minDist), 1.0);
}