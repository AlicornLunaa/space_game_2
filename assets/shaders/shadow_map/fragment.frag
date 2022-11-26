#ifdef GL_ES
precision mediump float;
#endif

struct Light {
    vec3 position;
    vec4 color;
    float intensity;
    float attenuation;
};

varying vec2 v_texcoord;
varying vec2 v_position;

uniform sampler2D u_occlusionMap;
uniform vec2 u_occlusionMapRes;
uniform vec2 u_lightMapRes;
uniform Light u_light;

void main(){
    vec4 color = vec4(1, 0, 0, 1);
    vec2 uv = (v_position + (u_lightMapRes / 2.0)) / u_lightMapRes;
    vec2 dirToLight = normalize(u_light.position.xy - uv);
    
    if(distance(uv, u_light.position.xy) <= 0.025){
        color = vec4(0, 1, 0, 1);
    }

    gl_FragColor = color * vec4(texture2D(u_occlusionMap, v_texcoord).rgb, 1.0);
}