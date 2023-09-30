attribute vec4 a_position;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;
uniform mat4 u_invCamTrans;
uniform float u_planetRadius;
uniform vec2 u_planetWorldPos;

varying vec2 v_texcoord;
varying vec2 v_worldcoord;

void main(){
    vec4 normalPos = u_projTrans * a_position;
    v_texcoord = a_texCoord0;
    v_worldcoord = (u_invCamTrans * normalPos).xy;

    // gl_Position = vec4(
    //     sin((normalPos.x / u_planetRadius) * 2.0 * 3.141592654) * normalPos.y,
    //     cos((normalPos.x / u_planetRadius) * 2.0 * 3.141592654) * normalPos.y,
    //     normalPos.z,
    //     normalPos.w 
    // );
    gl_Position = normalPos;
    // gl_Position.y = min(abs(u_invCamTrans.y), 50);
}   