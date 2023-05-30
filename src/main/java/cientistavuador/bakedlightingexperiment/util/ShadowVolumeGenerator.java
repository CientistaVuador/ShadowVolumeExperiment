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
package cientistavuador.bakedlightingexperiment.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector3f;

/**
 *
 * @author Cien
 */
public class ShadowVolumeGenerator {

    //must be flat shaded
    //the first 3 floats must be the position
    //**does not work**
    public static int[] generateShadowVolumeIndices(float[] vertices, int vertexSize, int[] indices) {
        //1-map the indices by the positions
        Map<Vector3f, List<Integer>> positionMap = new HashMap<>();
        for (int i = 0; i < indices.length; i++) {
            int vertexIndex = indices[i] * vertexSize;

            Vector3f position = new Vector3f(
                    vertices[vertexIndex + 0],
                    vertices[vertexIndex + 1],
                    vertices[vertexIndex + 2]
            );
            List<Integer> indexList = positionMap.get(position);
            if (indexList == null) {
                indexList = new ArrayList<>();
                positionMap.put(position, indexList);
            }
            indexList.add(indices[i]);
            if (indexList.size() > 2) {
                System.out.println("a");
            }
        }

        //2-generate quads between the triangles with the same positions.
        Vector3f cache = new Vector3f();
        int[] generatedIndices = new int[indices.length * 4];
        int generatedIndicesIndex = 0;
        for (int i = 0; i < indices.length / 3; i++) {
            int vertA = indices[(i * 3) + 0];
            int vertB = indices[(i * 3) + 1];
            int vertC = indices[(i * 3) + 2];

            if ((generatedIndicesIndex + 3) > generatedIndices.length) {
                generatedIndices = Arrays.copyOf(generatedIndices, (generatedIndices.length * 2) + 3);
            }

            generatedIndices[generatedIndicesIndex + 0] = vertA;
            generatedIndices[generatedIndicesIndex + 1] = vertB;
            generatedIndices[generatedIndicesIndex + 2] = vertC;

            generatedIndicesIndex += 3;

            //first triangle
            cache.set(
                    vertices[(vertB * vertexSize) + 0],
                    vertices[(vertB * vertexSize) + 1],
                    vertices[(vertB * vertexSize) + 2]
            );
            List<Integer> indexList = positionMap.get(cache);
            for (Integer vertX : indexList) {
                if (vertX == vertB) {
                    continue;
                }

                if ((generatedIndicesIndex + 3) > generatedIndices.length) {
                    generatedIndices = Arrays.copyOf(generatedIndices, (generatedIndices.length * 2) + 3);
                }
                
                generatedIndices[generatedIndicesIndex + 0] = vertA;
                generatedIndices[generatedIndicesIndex + 1] = vertX;
                generatedIndices[generatedIndicesIndex + 2] = vertB;
                
                generatedIndicesIndex += 3;
            }
            
            //second triangle
            cache.set(
                    vertices[(vertC * vertexSize) + 0],
                    vertices[(vertC * vertexSize) + 1],
                    vertices[(vertC * vertexSize) + 2]
            );
            indexList = positionMap.get(cache);
            for (Integer vertX : indexList) {
                if (vertX == vertC) {
                    continue;
                }

                if ((generatedIndicesIndex + 3) > generatedIndices.length) {
                    generatedIndices = Arrays.copyOf(generatedIndices, (generatedIndices.length * 2) + 3);
                }
                
                generatedIndices[generatedIndicesIndex + 0] = vertB;
                generatedIndices[generatedIndicesIndex + 1] = vertX;
                generatedIndices[generatedIndicesIndex + 2] = vertC;
                
                generatedIndicesIndex += 3;
            }
            
            //third triangle
            cache.set(
                    vertices[(vertA * vertexSize) + 0],
                    vertices[(vertA * vertexSize) + 1],
                    vertices[(vertA * vertexSize) + 2]
            );
            indexList = positionMap.get(cache);
            for (Integer vertX : indexList) {
                if (vertX == vertA) {
                    continue;
                }

                if ((generatedIndicesIndex + 3) > generatedIndices.length) {
                    generatedIndices = Arrays.copyOf(generatedIndices, (generatedIndices.length * 2) + 3);
                }
                
                generatedIndices[generatedIndicesIndex + 0] = vertC;
                generatedIndices[generatedIndicesIndex + 1] = vertX;
                generatedIndices[generatedIndicesIndex + 2] = vertA;
                
                generatedIndicesIndex += 3;
            }
        }

        //3-done
        return Arrays.copyOf(generatedIndices, generatedIndicesIndex);
    }

    private ShadowVolumeGenerator() {

    }
}
