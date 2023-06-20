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
package cientistavuador.shadowvolumeexperiment.clouds;

import cientistavuador.shadowvolumeexperiment.cube.VerticesStream;
import cientistavuador.shadowvolumeexperiment.resources.image.ImageResources;
import cientistavuador.shadowvolumeexperiment.resources.image.NativeImage;
import java.nio.ByteBuffer;

/**
 *
 * @author Cien
 */
public class CloudsMeshConvex {

    public static VerticesStream generateMesh() {
        NativeImage image = ImageResources.load("clouds.png", 0);
        try {
            return generateMesh(image);
        } finally {
            image.free();
        }
    }

    public static VerticesStream generateMesh(NativeImage image) {
        ByteBuffer imageData = image.getData();

        int width = image.getWidth();
        int height = image.getHeight();
        boolean[] data = new boolean[width * height];

        for (int i = 0; i < width * height; i++) {
            data[i] = (imageData.get(i * image.getChannels()) < 0);
        }

        return generateMesh(data, width, height);
    }

    public static VerticesStream generateMesh(boolean[] data, int width, int height) {
        VerticesStream stream = new VerticesStream();

        boolean[] processed = new boolean[width * height];
        
        for (int i = 0; i < width * height; i++) {
            if (!data[i]) {
                continue;
            }
            if (processed[i]) {
                continue;
            }
            processed[i] = true;

            int quadWidth = 1;
            int quadHeight = 1;

            int x = (i % width);
            int y = (i / width);

            boolean canGrowRight = true;
            boolean canGrowUp = true;

            while (canGrowRight || canGrowUp) {
                if (canGrowRight) {
                    if ((x + quadWidth) < (width - 1)) {
                        for (int j = 0; j < quadHeight; j++) {
                            if (!data[(x + quadWidth) + ((y + j) * width)] || processed[(x + quadWidth) + ((y + j) * width)]) {
                                canGrowRight = false;
                                break;
                            }
                        }
                        if (canGrowRight) {
                            for (int j = 0; j < quadHeight; j++) {
                                processed[(x + quadWidth) + ((y + j) * width)] = true;
                            }
                            quadWidth++;
                        }
                    } else {
                        canGrowRight = false;
                    }
                }

                if (canGrowUp) {
                    if ((y + quadHeight) < (height - 1)) {
                        for (int j = 0; j < quadWidth; j++) {
                            if (!data[(x + j) + ((y + quadHeight) * width)] || processed[(x + j) + ((y + quadHeight) * width)]) {
                                canGrowUp = false;
                                break;
                            }
                        }
                        if (canGrowUp) {
                            for (int j = 0; j < quadWidth; j++) {
                                processed[(x + j) + ((y + quadHeight) * width)] = true;
                            }
                            quadHeight++;
                        }
                    } else {
                        canGrowUp = false;
                    }
                }
            }

            float vX = x - (width / 2f);
            float vZ = -(y - (height / 2f));
            
            float scaleX = quadWidth;
            float scaleY = 1.0f;
            float scaleZ = quadHeight;

            float sizeX = scaleX;
            float sizeY = 0.5f * scaleY;
            float sizeZ = scaleZ;
            
            final float bias = 0.00005f;
            
            float xP = vX + sizeX - bias;
            float xN = vX + bias;
            float yP = sizeY - bias;
            float yN = -sizeY + bias;
            float zP = vZ - bias;
            float zN = vZ - sizeZ + bias;

            //TOP
            stream.offset();
            stream.vertex(xN, yP, zP, 0f, 1f, 0f, Float.NaN, Float.NaN, 0f, 0f);
            stream.vertex(xP, yP, zP, 0f, 1f, 0f, Float.NaN, Float.NaN, 1f, 0f);
            stream.vertex(xN, yP, zN, 0f, 1f, 0f, Float.NaN, Float.NaN, 0f, 1f);
            stream.vertex(xP, yP, zN, 0f, 1f, 0f, Float.NaN, Float.NaN, 1f, 1f);
            stream.quad(0, 3, 2, 0, 1, 3);

            //BOTTOM
            stream.offset();
            stream.vertex(xN, yN, zP, 0f, -1f, 0f, Float.NaN, Float.NaN, 1f, 0f);
            stream.vertex(xN, yN, zN, 0f, -1f, 0f, Float.NaN, Float.NaN, 1f, 1f);
            stream.vertex(xP, yN, zP, 0f, -1f, 0f, Float.NaN, Float.NaN, 0f, 0f);
            stream.vertex(xP, yN, zN, 0f, -1f, 0f, Float.NaN, Float.NaN, 0f, 1f);
            stream.quad(0, 3, 2, 0, 1, 3);

            //LEFT
            stream.offset();
            stream.vertex(xN, yP, zN, -1f, 0f, 0f, Float.NaN, Float.NaN, 0f, 1f);
            stream.vertex(xN, yN, zN, -1f, 0f, 0f, Float.NaN, Float.NaN, 0f, 0f);
            stream.vertex(xN, yN, zP, -1f, 0f, 0f, Float.NaN, Float.NaN, 1f, 0f);
            stream.vertex(xN, yP, zP, -1f, 0f, 0f, Float.NaN, Float.NaN, 1f, 1f);
            stream.quad(0, 1, 3, 1, 2, 3);

            //RIGHT
            stream.offset();
            stream.vertex(xP, yP, zN, 1f, 0f, 0f, Float.NaN, Float.NaN, 1f, 1f);
            stream.vertex(xP, yN, zP, 1f, 0f, 0f, Float.NaN, Float.NaN, 0f, 0f);
            stream.vertex(xP, yN, zN, 1f, 0f, 0f, Float.NaN, Float.NaN, 1f, 0f);
            stream.vertex(xP, yP, zP, 1f, 0f, 0f, Float.NaN, Float.NaN, 0f, 1f);
            stream.quad(0, 3, 2, 2, 3, 1);

            //FRONT
            stream.offset();
            stream.vertex(xN, yP, zN, 0f, 0f, -1f, Float.NaN, Float.NaN, 1f, 1f);
            stream.vertex(xP, yN, zN, 0f, 0f, -1f, Float.NaN, Float.NaN, 0f, 0f);
            stream.vertex(xN, yN, zN, 0f, 0f, -1f, Float.NaN, Float.NaN, 1f, 0f);
            stream.vertex(xP, yP, zN, 0f, 0f, -1f, Float.NaN, Float.NaN, 0f, 1f);
            stream.quad(0, 3, 2, 3, 1, 2);

            //BACK
            stream.offset();
            stream.vertex(xN, yP, zP, 0f, 0f, 1f, Float.NaN, Float.NaN, 0f, 1f);
            stream.vertex(xN, yN, zP, 0f, 0f, 1f, Float.NaN, Float.NaN, 0f, 0f);
            stream.vertex(xP, yN, zP, 0f, 0f, 1f, Float.NaN, Float.NaN, 1f, 0f);
            stream.vertex(xP, yP, zP, 0f, 0f, 1f, Float.NaN, Float.NaN, 1f, 1f);
            stream.quad(0, 1, 3, 3, 1, 2);
        }

        return stream;
    }

    private CloudsMeshConvex() {

    }
}
