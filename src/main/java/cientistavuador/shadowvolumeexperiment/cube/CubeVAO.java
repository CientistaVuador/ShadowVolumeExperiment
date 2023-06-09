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

import cientistavuador.shadowvolumeexperiment.util.ShadowVolumeGenerator;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class CubeVAO {

    public static final int VERTEX_SIZE_ELEMENTS = 3 + 3 + 2;
    public static final int VAO;
    public static final int CUBE_COUNT;
    public static final int CUBE_OFFSET;
    public static final int CUBE_SHADOW_VOLUME_COUNT;
    public static final int CUBE_SHADOW_VOLUME_OFFSET;

    static {
        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);
        
        VerticesStream stream = generateVertices();
        
        float[] vertices = stream.vertices();
        
        int[] indices = stream.indices();
        int[] volumeIndices = ShadowVolumeGenerator.generateShadowVolumeIndices(vertices, VERTEX_SIZE_ELEMENTS, indices);
        
        CUBE_COUNT = indices.length;
        CUBE_OFFSET = 0;
        CUBE_SHADOW_VOLUME_COUNT = volumeIndices.length;
        CUBE_SHADOW_VOLUME_OFFSET = indices.length * Integer.BYTES;
        
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
        glVertexAttribPointer(0, 3, GL_FLOAT, false, VERTEX_SIZE_ELEMENTS * Float.BYTES, 0);

        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, VERTEX_SIZE_ELEMENTS * Float.BYTES, (3) * Float.BYTES);

        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, VERTEX_SIZE_ELEMENTS * Float.BYTES, (3 + 3) * Float.BYTES);

        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 2, GL_FLOAT, false, VERTEX_SIZE_ELEMENTS * Float.BYTES, (3 + 3 + 2) * Float.BYTES);

        glEnableVertexAttribArray(4);
        glVertexAttribPointer(4, 2, GL_FLOAT, false, VERTEX_SIZE_ELEMENTS * Float.BYTES, (3 + 3 + 2 + 2) * Float.BYTES);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glBindVertexArray(0);
    }

    private static VerticesStream generateVertices() {
        VerticesStream stream = new VerticesStream(CubeTexture.TEXTURE_WIDTH, CubeTexture.TEXTURE_HEIGHT);

        float scaleX = 1.0f;
        float scaleY = 1.0f;
        float scaleZ = 1.0f;

        float sizeX = 0.5f * scaleX;
        float sizeY = 0.5f * scaleY;
        float sizeZ = 0.5f * scaleZ;

        float xP = sizeX;
        float xN = -sizeX;
        float yP = sizeY;
        float yN = -sizeY;
        float zP = sizeZ;
        float zN = -sizeZ;

        //TOP
        stream.offset();
        stream.vertex(xN, yP, zP, 0f, 1f, 0f, 85f, 352f, 0f, 0f);
        stream.vertex(xP, yP, zP, 0f, 1f, 0f, 212f, 352f, 1f, 0f);
        stream.vertex(xN, yP, zN, 0f, 1f, 0f, 85f, 479f, 0f, 1f);
        stream.vertex(xP, yP, zN, 0f, 1f, 0f, 212f, 479f, 1f, 1f);
        stream.quad(0, 3, 2, 0, 1, 3);

        //BOTTOM
        stream.offset();
        stream.vertex(xN, yN, zP, 0f, -1f, 0f, 425f, 352f, 1f, 0f);
        stream.vertex(xN, yN, zN, 0f, -1f, 0f, 425f, 479f, 1f, 1f);
        stream.vertex(xP, yN, zP, 0f, -1f, 0f, 298f, 352f, 0f, 0f);
        stream.vertex(xP, yN, zN, 0f, -1f, 0f, 298f, 479f, 0f, 1f);
        stream.quad(0, 3, 2, 0, 1, 3);

        //LEFT
        stream.offset();
        stream.vertex(xN, yP, zN, -1f, 0f, 0f, 85f, 319f, 0f, 1f);
        stream.vertex(xN, yN, zN, -1f, 0f, 0f, 85f, 192f, 0f, 0f);
        stream.vertex(xN, yN, zP, -1f, 0f, 0f, 212f, 192f, 1f, 0f);
        stream.vertex(xN, yP, zP, -1f, 0f, 0f, 212f, 319f, 1f, 1f);
        stream.quad(0, 1, 3, 1, 2, 3);

        //RIGHT
        stream.offset();
        stream.vertex(xP, yP, zN, 1f, 0f, 0f, 425f, 319f, 1f, 1f);
        stream.vertex(xP, yN, zP, 1f, 0f, 0f, 298f, 192f, 0f, 0f);
        stream.vertex(xP, yN, zN, 1f, 0f, 0f, 425f, 192f, 1f, 0f);
        stream.vertex(xP, yP, zP, 1f, 0f, 0f, 298f, 319f, 0f, 1f);
        stream.quad(0, 3, 2, 2, 3, 1);

        //FRONT
        stream.offset();
        stream.vertex(xN, yP, zN, 0f, 0f, -1f, 212f, 159f, 1f, 1f);
        stream.vertex(xP, yN, zN, 0f, 0f, -1f, 85f, 32f, 0f, 0f);
        stream.vertex(xN, yN, zN, 0f, 0f, -1f, 212f, 32f, 1f, 0f);
        stream.vertex(xP, yP, zN, 0f, 0f, -1f, 85f, 159f, 0f, 1f);
        stream.quad(0, 3, 2, 3, 1, 2);

        //BACK
        stream.offset();
        stream.vertex(xN, yP, zP, 0f, 0f, 1f, 298f, 159f, 0f, 1f);
        stream.vertex(xN, yN, zP, 0f, 0f, 1f, 298f, 32f, 0f, 0f);
        stream.vertex(xP, yN, zP, 0f, 0f, 1f, 425f, 32f, 1f, 0f);
        stream.vertex(xP, yP, zP, 0f, 0f, 1f, 425f, 159f, 1f, 1f);
        stream.quad(0, 1, 3, 3, 1, 2);

        return stream;
    }

    private CubeVAO() {

    }

}
