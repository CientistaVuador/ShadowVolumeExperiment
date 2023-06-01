/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <https://unlicense.org>
 */
package cientistavuador.shadowvolumeexperiment.skybox;

import cientistavuador.shadowvolumeexperiment.cube.light.directional.DirectionalLight;
import cientistavuador.shadowvolumeexperiment.util.BetterUniformSetter;
import cientistavuador.shadowvolumeexperiment.util.ProgramCompiler;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class SkyBoxProgram {
    
    public static final String VERTEX_SHADER = 
            """
            #version 330 core
            
            uniform mat4 projectionView;
            uniform vec3 camPosition;
            
            layout (location = 0) in vec3 vertexPosition;
            
            out vec3 fragPosition;
            
            void main() {
                vec4 worldPosition = vec4(vertexPosition + camPosition, 1.0);
                fragPosition = worldPosition.xyz;
                vec4 position = projectionView * worldPosition;
                gl_Position = position.xyww;
            }
            """;
    
    public static final String FRAGMENT_SHADER =
            """
            #version 330 core
            
            uniform vec3 camPosition;
            
            uniform vec3 lightDirection;
            uniform vec3 lightDiffuse;
            uniform vec3 lightSpecular;
            uniform vec3 lightAmbient;
            
            in vec3 fragPosition;
            
            layout (location = 0) out vec4 colorOutput;
            
            void main() {
                const vec3 skyColor = vec3(0.2, 0.4, 0.6);
                
                vec3 viewDir = normalize(camPosition - fragPosition);
                vec3 normal = viewDir;
                vec3 reflectDir = -reflect(lightDirection, normal);
                
                vec3 resultColor = vec3(0.0);
                
                resultColor += lightDiffuse * skyColor;
                resultColor += lightSpecular * pow(max(dot(viewDir, reflectDir), 0.0), 1024.0) * skyColor.bgr * skyColor.bgr * 5.0;
                resultColor += lightSpecular * pow(max(dot(viewDir, reflectDir), 0.0), 32.0) * skyColor.bgr * skyColor.bgr * 1.25;
                resultColor += lightAmbient * skyColor;
                
                colorOutput = vec4(resultColor, 1.0);
            }
            """;
    
    public static final int SHADER_PROGRAM = ProgramCompiler.compile(VERTEX_SHADER, FRAGMENT_SHADER);
    private static final BetterUniformSetter UNIFORMS = new BetterUniformSetter(SHADER_PROGRAM,
            "projectionView",
            "camPosition",
            "lightDirection",
            "lightDiffuse",
            "lightSpecular",
            "lightAmbient"
    );
    
    public static void sendUniforms(Matrix4fc projectionView, Vector3f camPos, DirectionalLight sun) {
        BetterUniformSetter.uniformMatrix4fv(UNIFORMS.locationOf("projectionView"), projectionView);
        glUniform3f(UNIFORMS.locationOf("camPosition"), camPos.x(), camPos.y(), camPos.z());
        
        glUniform3f(UNIFORMS.locationOf("lightDirection"), sun.getDirection().x(), sun.getDirection().y(), sun.getDirection().z());
        glUniform3f(UNIFORMS.locationOf("lightDiffuse"), sun.getDiffuseColor().x(), sun.getDiffuseColor().y(), sun.getDiffuseColor().z());
        glUniform3f(UNIFORMS.locationOf("lightSpecular"), sun.getSpecularColor().x(), sun.getSpecularColor().y(), sun.getSpecularColor().z());
        glUniform3f(UNIFORMS.locationOf("lightAmbient"), sun.getAmbientColor().x(), sun.getAmbientColor().y(), sun.getAmbientColor().z());
        
    }
    
    private SkyBoxProgram() {
        
    }
}
