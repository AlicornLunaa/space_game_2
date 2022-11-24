attribute vec4 a_position;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;
uniform mat4 u_invProjViewMatrix;

varying vec2 v_texcoord;
varying vec3 v_viewvector;

void main(){
    v_texcoord = a_texCoord0;
    v_viewvector = vec3(u_invProjViewMatrix * vec4(a_texCoord0.xy * 2.0 - 1.0, 0.0, -1.0));
    
    gl_Position = u_projTrans * a_position;
}