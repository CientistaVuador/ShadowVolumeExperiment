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
package cientistavuador.shadowvolumeexperiment.rifle;

import cientistavuador.shadowvolumeexperiment.cube.CubeVAO;
import cientistavuador.shadowvolumeexperiment.util.ShadowVolumeGenerator;
import java.util.Map;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class RifleVAO {
    public static final int VAO;
    public static final int TEAPOT_COUNT;
    public static final int TEAPOT_OFFSET;
    public static final int TEAPOT_SHADOW_VOLUME_COUNT;
    public static final int TEAPOT_SHADOW_VOLUME_OFFSET;

    static {
        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);
        
        Map.Entry<float[], int[]> mesh = RifleMesh.readMesh();
        
        float[] vertices = mesh.getKey();
        
        int[] indices = mesh.getValue();
        int[] volumeIndices = ShadowVolumeGenerator.generateShadowVolumeIndices(vertices, CubeVAO.VERTEX_SIZE_ELEMENTS, indices);
        
        TEAPOT_COUNT = indices.length;
        TEAPOT_OFFSET = 0;
        TEAPOT_SHADOW_VOLUME_COUNT = volumeIndices.length;
        TEAPOT_SHADOW_VOLUME_OFFSET = indices.length * Integer.BYTES;
        
        int[] mixedIndices = new int[indices.length + volumeIndices.length];
        System.arraycopy(indices, 0, mixedIndices, 0, indices.length);
        System.arraycopy(volumeIndices, 0, mixedIndices, indices.length, volumeIndices.length);

        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, mixedIndices, GL_STATIC_DRAW);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, CubeVAO.VERTEX_SIZE_ELEMENTS * Float.BYTES, 0);

        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, CubeVAO.VERTEX_SIZE_ELEMENTS * Float.BYTES, (3) * Float.BYTES);

        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, CubeVAO.VERTEX_SIZE_ELEMENTS * Float.BYTES, (3 + 3) * Float.BYTES);

        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 2, GL_FLOAT, false, CubeVAO.VERTEX_SIZE_ELEMENTS * Float.BYTES, (3 + 3 + 2) * Float.BYTES);

        glEnableVertexAttribArray(4);
        glVertexAttribPointer(4, 2, GL_FLOAT, false, CubeVAO.VERTEX_SIZE_ELEMENTS * Float.BYTES, (3 + 3 + 2 + 2) * Float.BYTES);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glBindVertexArray(0);
    }

    private RifleVAO() {

    }
}
