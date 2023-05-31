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
import static org.lwjgl.opengl.GL33C.*;
import org.lwjgl.system.MemoryStack;

/**
 *
 * @author Cien
 */
public class CubeShadowVolumeProgram {
    public static final String VERTEX_SHADER
            = 
            """
            #version 330 core
            
            uniform mat4 projectionView;
            uniform mat4 model;
            uniform mat3 normalModel;
            
            uniform vec3 lightDirection;
            
            layout (location = 0) in vec3 vertexPosition;
            layout (location = 1) in vec3 vertexNormal;
            
            void main() {
                vec4 outputPosition = model * vec4(vertexPosition, 1.0);
                outputPosition /= outputPosition.w;
                vec3 normal = normalize(normalModel * vertexNormal);
                
                if (dot(lightDirection, normal) > 0) {
                    outputPosition.xyz += lightDirection * 500.0;
                }
                
                gl_Position = projectionView * outputPosition;
            }
            """;

    public static final String FRAGMENT_SHADER
            = 
            """
            #version 330 core
            
            layout (location = 0) out vec4 outputColor;
            
            void main() {
                outputColor = vec4(vec3(1.0, 211.0 / 255.0, 0), 0.25);
            }
            """;

    public static final int SHADER_PROGRAM = ProgramCompiler.compile(VERTEX_SHADER, FRAGMENT_SHADER);
    public static final int PROJECTION_VIEW_INDEX = glGetUniformLocation(SHADER_PROGRAM, "projectionView");
    public static final int MODEL_INDEX = glGetUniformLocation(SHADER_PROGRAM, "model");
    public static final int NORMAL_MODEL_INDEX = glGetUniformLocation(SHADER_PROGRAM, "normalModel");
    
    public static final int LIGHT_DIRECTION_INDEX = glGetUniformLocation(SHADER_PROGRAM, "lightDirection");
    
    private static void sendMatrix(int location, Matrix4fc matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer matrixBuffer = stack.mallocFloat(4 * 4);
            matrix.get(matrixBuffer);
            glUniformMatrix4fv(location, false, matrixBuffer);
        }
    }

    public static void sendPerFrameUniforms(Matrix4fc projectionView, DirectionalLight light) {
        sendMatrix(PROJECTION_VIEW_INDEX, projectionView);
        
        glUniform3f(
                LIGHT_DIRECTION_INDEX,
                light.getDirection().x(), light.getDirection().y(), light.getDirection().z()
        );
    }

    public static void sendPerDrawUniforms(Matrix4fc model, Matrix3fc normalModel) {
        sendMatrix(MODEL_INDEX, model);
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer matrixBuffer = stack.mallocFloat(3 * 3);
            normalModel.get(matrixBuffer);
            glUniformMatrix3fv(NORMAL_MODEL_INDEX, false, matrixBuffer);
        }
    }

    private CubeShadowVolumeProgram() {

    }
}
