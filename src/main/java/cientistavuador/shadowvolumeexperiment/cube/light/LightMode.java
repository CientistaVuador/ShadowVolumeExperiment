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
package cientistavuador.shadowvolumeexperiment.cube.light;

/**
 *
 * @author Cien
 */
public class LightMode {
    private boolean diffuse = true;
    private boolean specular = true;
    private boolean ambient = true;
    
    public LightMode() {
        
    }
    
    public void enableAll() {
        this.diffuse = true;
        this.specular = true;
        this.ambient = true;
    }
    
    public void disableAll() {
        this.diffuse = false;
        this.specular = false;
        this.ambient = false;
    }
    
    public void ambientOnly() {
        this.diffuse = false;
        this.specular = false;
        this.ambient = true;
    }
    
    public void diffuseSpecularOnly() {
        this.diffuse = true;
        this.specular = true;
        this.ambient = false;
    }
    
    public boolean diffuse() {
        return this.diffuse;
    }
    
    public boolean specular() {
        return this.specular;
    }
    
    public boolean ambient() {
        return this.ambient;
    }
    
    public void diffuse(boolean enabled) {
        this.diffuse = enabled;
    }
    
    public void specular(boolean enabled) {
        this.specular = enabled;
    }
    
    public void ambient(boolean enabled) {
        this.ambient = enabled;
    }
}
