attribute vec4 a_position;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;
uniform mat4 u_invCamTrans;
varying vec2 v_texcoord;
varying vec2 v_worldcoord;

void main(){
    v_texcoord = a_texCoord0;
    gl_Position = u_projTrans * a_position;
    v_worldcoord = (u_invCamTrans * gl_Position).xy;
}