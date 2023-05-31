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
package cientistavuador.shadowvolumeexperiment.cube;

import cientistavuador.shadowvolumeexperiment.cube.light.directional.DirectionalLight;
import cientistavuador.shadowvolumeexperiment.util.ProgramCompiler;
import java.nio.FloatBuffer;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import static org.lwjgl.opengl.GL33C.*;
import org.lwjgl.system.MemoryStack;

/**
 *
 * @author Cien
 */
public class CubeProgram {

    public static final String VERTEX_SHADER
            = 
            """
            #version 330 core
            
            uniform mat4 projectionView;
            uniform mat4 model;
            uniform mat3 normalModel;
            
            layout (location = 0) in vec3 vertexPosition;
            layout (location = 1) in vec3 vertexNormal;
            layout (location = 2) in vec2 vertexTexture;
            
            out vec3 fragPosition;
            out vec3 fragNormal;
            out vec2 texCoords;
            
            void main() {
                texCoords = vertexTexture;
                fragNormal = normalize(normalModel * vertexNormal);
            
                vec4 outputPosition = model * vec4(vertexPosition, 1.0);
                outputPosition /= outputPosition.w;
                fragPosition = outputPosition.xyz;
                
                gl_Position = projectionView * outputPosition;
            }
            """;

    public static final String FRAGMENT_SHADER
            = 
            """
            #version 330 core
            
            uniform sampler2D cubeTexture;
            uniform sampler2D cubeTextureSpecular;
            
            uniform vec3 camPosition;
            
            uniform vec3 lightDirection;
            uniform vec3 lightDiffuse;
            uniform vec3 lightSpecular;
            uniform vec3 lightAmbient;
            
            in vec3 fragPosition;
            in vec3 fragNormal;
            in vec2 texCoords;
            
            layout (location = 0) out vec4 outputColor;
            
            void main() {
                bool noTexCoords = isnan(texCoords.x) || isnan(texCoords.y);
                
                vec4 textureColor = vec4((fragNormal + 1.0) / 2.0, 1.0);
                if (!noTexCoords) {
                    textureColor = texture(cubeTexture, texCoords);
                }
                textureColor.rgb = pow(textureColor.rgb, vec3(2.2));
                
                vec4 specularColor = vec4(1.0);
                if (!noTexCoords) {
                    specularColor = texture(cubeTextureSpecular, texCoords);
                }
                specularColor.rgb = pow(specularColor.rrr, vec3(2.2));
                
                vec3 viewDir = normalize(camPosition - fragPosition);
                vec3 reflectDir = reflect(lightDirection, fragNormal);
                
                vec3 resultColor = vec3(0.0);
                
                resultColor += lightDiffuse * max(dot(fragNormal, -lightDirection), 0.0) * textureColor.rgb;
                resultColor += lightSpecular * pow(max(dot(viewDir, reflectDir), 0.0), 32.0) * specularColor.rgb;
                resultColor += lightAmbient * textureColor.rgb;
                
                resultColor = pow(resultColor, vec3(1.0/2.2));
                outputColor = vec4(resultColor, textureColor.a);
            }
            """;

    public static final int SHADER_PROGRAM = ProgramCompiler.compile(VERTEX_SHADER, FRAGMENT_SHADER);
    public static final int PROJECTION_VIEW_INDEX = glGetUniformLocation(SHADER_PROGRAM, "projectionView");
    public static final int MODEL_INDEX = glGetUniformLocation(SHADER_PROGRAM, "model");
    public static final int NORMAL_MODEL_INDEX = glGetUniformLocation(SHADER_PROGRAM, "normalModel");
    public static final int CUBE_TEXTURE_INDEX = glGetUniformLocation(SHADER_PROGRAM, "cubeTexture");
    public static final int CUBE_TEXTURE_SPECULAR_INDEX = glGetUniformLocation(SHADER_PROGRAM, "cubeTextureSpecular");

    public static final int LIGHT_DIRECTION_INDEX = glGetUniformLocation(SHADER_PROGRAM, "lightDirection");
    public static final int LIGHT_DIFFUSE_INDEX = glGetUniformLocation(SHADER_PROGRAM, "lightDiffuse");
    public static final int LIGHT_SPECULAR_INDEX = glGetUniformLocation(SHADER_PROGRAM, "lightSpecular");
    public static final int LIGHT_AMBIENT_INDEX = glGetUniformLocation(SHADER_PROGRAM, "lightAmbient");

    public static final int CAM_POSITION_INDEX = glGetUniformLocation(SHADER_PROGRAM, "camPosition");
    
    private static void sendMatrix(int location, Matrix4fc matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer matrixBuffer = stack.mallocFloat(4 * 4);
            matrix.get(matrixBuffer);
            glUniformMatrix4fv(location, false, matrixBuffer);
        }
    }

    public static void sendPerFrameUniforms(int cubeTexture, int cubeTextureSpecular, Matrix4fc projectionView, Vector3f camPosition, DirectionalLight light) {
        sendMatrix(PROJECTION_VIEW_INDEX, projectionView);
        glUniform3f(CAM_POSITION_INDEX, camPosition.x(), camPosition.y(), camPosition.z());
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, cubeTexture);
        glUniform1i(CUBE_TEXTURE_INDEX, 0);
        
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, cubeTextureSpecular);
        glUniform1i(CUBE_TEXTURE_SPECULAR_INDEX, 1);

        glUniform3f(
                LIGHT_DIRECTION_INDEX,
                light.getDirection().x(), light.getDirection().y(), light.getDirection().z()
        );

        glUniform3f(LIGHT_DIFFUSE_INDEX, 0f, 0f, 0f);
        glUniform3f(LIGHT_SPECULAR_INDEX, 0f, 0f, 0f);
        glUniform3f(LIGHT_AMBIENT_INDEX, 0f, 0f, 0f);

        if (light.getLightMode().diffuse()) {
            glUniform3f(
                    LIGHT_DIFFUSE_INDEX,
                    light.getDiffuseColor().x(), light.getDiffuseColor().y(), light.getDiffuseColor().z()
            );
        }
        
        if (light.getLightMode().specular()) {
            glUniform3f(
                    LIGHT_SPECULAR_INDEX,
                    light.getSpecularColor().x(), light.getSpecularColor().y(), light.getSpecularColor().z()
            );
        }
        
        if (light.getLightMode().ambient()) {
            glUniform3f(
                    LIGHT_AMBIENT_INDEX,
                    light.getAmbientColor().x(), light.getAmbientColor().y(), light.getAmbientColor().z()
            );
        }
    }

    public static void sendPerDrawUniforms(Matrix4fc model, Matrix3fc normalModel) {
        sendMatrix(MODEL_INDEX, model);
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer matrixBuffer = stack.mallocFloat(3 * 3);
            normalModel.get(matrixBuffer);
            glUniformMatrix3fv(NORMAL_MODEL_INDEX, false, matrixBuffer);
        }
    }

    private CubeProgram() {

    }

}
