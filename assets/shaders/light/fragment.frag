#ifdef GL_ES
precision mediump float;
#endif

#define PI 3.141592654

varying vec2 v_texcoord;

uniform sampler2D u_texture;
uniform vec2 u_resolution;

float sample(vec2 coord, float r){
    return step(r, texture2D(u_texture, coord).r);
}

void main(){
    // Cartesian to polar transform
    vec2 norm = v_texcoord.st * 2.0 - 1.0;
    float theta = atan(norm.y, norm.x);
    float r = length(norm);
    float coord = (theta + PI) / (2.0 * PI);

    vec2 tc = vec2(coord, 0.0);
    float center = sample(tc, r);
    float blur = (1.0 / u_resolution.x) * smoothstep(0.0, 1.0, r);

    float sum = 0.0;
	sum += sample(vec2(tc.x - 4.0*blur, tc.y), r) * 0.05;
	sum += sample(vec2(tc.x - 3.0*blur, tc.y), r) * 0.09;
	sum += sample(vec2(tc.x - 2.0*blur, tc.y), r) * 0.12;
	sum += sample(vec2(tc.x - 1.0*blur, tc.y), r) * 0.15;
	
	sum += center * 0.16;
	
	sum += sample(vec2(tc.x + 1.0*blur, tc.y), r) * 0.15;
	sum += sample(vec2(tc.x + 2.0*blur, tc.y), r) * 0.12;
	sum += sample(vec2(tc.x + 3.0*blur, tc.y), r) * 0.09;
	sum += sample(vec2(tc.x + 4.0*blur, tc.y), r) * 0.05;

    gl_FragColor = vec4(vec3(1.0), sum * smoothstep(1.0, 0.0, r));
}