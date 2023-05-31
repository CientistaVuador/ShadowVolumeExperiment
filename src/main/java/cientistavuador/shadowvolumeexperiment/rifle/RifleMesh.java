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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;
import org.joml.Vector3f;

/**
 *
 * @author Cien
 */
public class RifleMesh {

    public static Map.Entry<float[], int[]> readMesh() {
        try {
            try (
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    RifleMesh.class.getResourceAsStream("rifle.obj"),
                                    StandardCharsets.UTF_8
                            )
                    )) {

                float[] positions = new float[64];
                int positionsIndex = 0;

                float[] vertices = new float[64];
                int verticesIndex = 0;

                Vector3f vertAPos = new Vector3f();
                Vector3f vertBPos = new Vector3f();
                Vector3f vertCPos = new Vector3f();

                Vector3f A = new Vector3f();
                Vector3f B = new Vector3f();
                Vector3f N = new Vector3f();

                String s;
                while ((s = reader.readLine()) != null) {
                    if (s.isBlank()) {
                        continue;
                    }

                    String[] split = s.split(Pattern.quote(" "));

                    switch (split[0]) {
                        case "v" -> {
                            if ((positionsIndex + 3) > positions.length) {
                                positions = Arrays.copyOf(positions, (positions.length * 2) + 3);
                            }

                            positions[positionsIndex + 0] = Float.parseFloat(split[1]);
                            positions[positionsIndex + 1] = Float.parseFloat(split[2]);
                            positions[positionsIndex + 2] = Float.parseFloat(split[3]);

                            positionsIndex += 3;
                        }
                        case "f" -> {
                            int vertA = Integer.parseInt(split[1]) - 1;
                            int vertB = Integer.parseInt(split[2]) - 1;
                            int vertC = Integer.parseInt(split[3]) - 1;

                            vertAPos.set(
                                    positions[(vertA * 3) + 0],
                                    positions[(vertA * 3) + 1],
                                    positions[(vertA * 3) + 2]
                            );
                            vertBPos.set(
                                    positions[(vertB * 3) + 0],
                                    positions[(vertB * 3) + 1],
                                    positions[(vertB * 3) + 2]
                            );
                            vertCPos.set(
                                    positions[(vertC * 3) + 0],
                                    positions[(vertC * 3) + 1],
                                    positions[(vertC * 3) + 2]
                            );

                            A.set(vertBPos).sub(vertAPos);
                            B.set(vertCPos).sub(vertAPos);
                            N.set(A).cross(B).normalize();

                            Vector3f vert = vertAPos;
                            for (int i = 0; i < 3; i++) {
                                switch (i) {
                                    case 0 -> vert = vertAPos;
                                    case 1 -> vert = vertBPos;
                                    case 2 -> vert = vertCPos;
                                }
                                
                                if ((verticesIndex + CubeVAO.VERTEX_SIZE_ELEMENTS) > vertices.length) {
                                    vertices = Arrays.copyOf(vertices, (vertices.length * 2) + CubeVAO.VERTEX_SIZE_ELEMENTS);
                                }

                                vertices[verticesIndex + 0] = vert.x();
                                vertices[verticesIndex + 1] = vert.y();
                                vertices[verticesIndex + 2] = vert.z();
                                vertices[verticesIndex + 3] = N.x();
                                vertices[verticesIndex + 4] = N.y();
                                vertices[verticesIndex + 5] = N.z();
                                vertices[verticesIndex + 6] = Float.NaN;
                                vertices[verticesIndex + 7] = Float.NaN;
                                
                                verticesIndex += CubeVAO.VERTEX_SIZE_ELEMENTS;
                            }
                        }
                        default -> {
                            //System.err.println("'" + split[0] + "' is not supported.");
                        }
                    }
                }
                
                float[] compactVertices = Arrays.copyOf(vertices, verticesIndex);
                
                int[] indices = new int[compactVertices.length / CubeVAO.VERTEX_SIZE_ELEMENTS];
                for (int i = 0; i < indices.length; i++) {
                    indices[i] = i;
                }

                return Map.entry(
                        compactVertices,
                        indices
                );
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private RifleMesh() {

    }

}
