varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float temperature;

void main(){
    if(length(v_texCoords - vec2(0.5, 0.5)) <= 0.5){
        gl_FragColor = v_color * vec4(1, 0.8, 0, 1);
    } else {
        gl_FragColor = vec4(0, 0, 0, 0);
    }
}