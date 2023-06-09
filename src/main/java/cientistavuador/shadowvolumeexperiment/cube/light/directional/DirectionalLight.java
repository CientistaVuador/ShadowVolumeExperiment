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
package cientistavuador.shadowvolumeexperiment.cube.light.directional;

import cientistavuador.shadowvolumeexperiment.cube.light.Light;
import cientistavuador.shadowvolumeexperiment.cube.light.LightMode;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 *
 * @author Cien
 */
public class DirectionalLight implements Light {
    
    private static final Vector3f position = new Vector3f(0f);
    
    private final Vector3f direction = new Vector3f(-0.5f, -1f, 0.5f).normalize();
    private final Vector3f iconColor = new Vector3f(255f / 255f, 253f / 255f, 242f / 255f);
    private final Vector3f diffuseColor = new Vector3f(iconColor).mul(1.00f, 1.05f, 1.10f);
    private final Vector3f specularColor = new Vector3f(iconColor).mul(1.25f, 1.25f, 1.25f);
    private final Vector3f ambientColor = new Vector3f(iconColor).mul(0.3f, 0.35f, 0.40f);
    private final LightMode lightMode = new LightMode();
    
    public DirectionalLight(Vector3fc direction) {
        if (direction != null) {
            this.direction.set(direction);
        }
    }
    
    public DirectionalLight() {
        this(null);
    }

    @Override
    public Vector3fc getPosition() {
        return DirectionalLight.position;
    }
    
    public Vector3f getDirection() {
        return direction;
    }

    @Override
    public Vector3f getDiffuseColor() {
        return diffuseColor;
    }
    
    @Override
    public Vector3f getSpecularColor() {
        return specularColor;
    }
    
    @Override
    public Vector3f getAmbientColor() {
        return ambientColor;
    }

    @Override
    public LightMode getLightMode() {
        return lightMode;
    }
    
}
