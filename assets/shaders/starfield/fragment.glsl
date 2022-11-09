varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float x;
uniform float y;

void main(){
    gl_FragColor = v_color * texture2D(u_texture, vec2(v_texCoords.x + x, v_texCoords.y));
}