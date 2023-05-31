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
package cientistavuador.shadowvolumeexperiment;

import cientistavuador.shadowvolumeexperiment.camera.FreeCamera;
import cientistavuador.shadowvolumeexperiment.cube.Cube;
import cientistavuador.shadowvolumeexperiment.cube.CubeProgram;
import cientistavuador.shadowvolumeexperiment.cube.CubeShadowVolumeProgram;
import cientistavuador.shadowvolumeexperiment.cube.light.directional.DirectionalLight;
import cientistavuador.shadowvolumeexperiment.ubo.CameraUBO;
import cientistavuador.shadowvolumeexperiment.ubo.UBOBindingPoints;
import cientistavuador.shadowvolumeexperiment.debug.AabRender;
import cientistavuador.shadowvolumeexperiment.text.GLFontRenderer;
import cientistavuador.shadowvolumeexperiment.text.GLFontSpecification;
import cientistavuador.shadowvolumeexperiment.text.GLFontSpecifications;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class Game {

    private static final Game GAME = new Game();

    public static Game get() {
        return GAME;
    }

    private final FreeCamera camera = new FreeCamera();
    private final DirectionalLight sun = new DirectionalLight();
    private final List<Cube> cubes = new ArrayList<>();
    private boolean textEnabled = true;
    private boolean showShadowVolumes = false;

    private Game() {

    }

    public void start() {
        camera.setPosition(0, 1f, 25f);
        camera.setUBO(CameraUBO.create(UBOBindingPoints.PLAYER_CAMERA));

        Matrix4f model = new Matrix4f()
                .translate(0f, -0.5f, 0f)
                .scale(50f, 1f, 50f);
        cubes.add(new Cube(model));
    }

    private void renderShadowVolumes() {
        Matrix4f cameraProjectionView = new Matrix4f(this.camera.getProjectionView());

        glUseProgram(CubeShadowVolumeProgram.SHADER_PROGRAM);
        CubeShadowVolumeProgram.sendPerFrameUniforms(cameraProjectionView, sun);

        glBindVertexArray(Cube.VAO);
        for (Cube c : cubes) {
            CubeShadowVolumeProgram.sendPerDrawUniforms(c.getModel(), c.getNormalModel());
            glDrawElements(GL_TRIANGLES, Cube.CUBE_SHADOW_VOLUME_COUNT, GL_UNSIGNED_INT, Cube.CUBE_SHADOW_VOLUME_OFFSET);

            Main.NUMBER_OF_DRAWCALLS++;
            Main.NUMBER_OF_VERTICES += Cube.CUBE_SHADOW_VOLUME_COUNT;
        }
        glBindVertexArray(0);

        glUseProgram(0);
    }

    public void loop() {
        camera.updateMovement();
        Matrix4f cameraProjectionView = new Matrix4f(this.camera.getProjectionView());

        if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_SPACE) == GLFW_PRESS) {
            this.sun.getDirection().set(this.camera.getFront());
        }

        glStencilFunc(GL_ALWAYS, 0, 0xFF);
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
        
        //render ambient only
        sun.getLightMode().ambientOnly();
        glUseProgram(Cube.SHADER_PROGRAM);
        CubeProgram.sendPerFrameUniforms(Cube.CUBE_TEXTURE, Cube.CUBE_TEXTURE_SPECULAR, cameraProjectionView, new Vector3f().set(camera.getPosition()), sun);
        glBindVertexArray(Cube.VAO);
        for (Cube c : cubes) {
            CubeProgram.sendPerDrawUniforms(c.getModel(), c.getNormalModel());
            glDrawElements(GL_TRIANGLES, Cube.CUBE_COUNT, GL_UNSIGNED_INT, Cube.CUBE_OFFSET);

            Main.NUMBER_OF_DRAWCALLS++;
            Main.NUMBER_OF_VERTICES += Cube.CUBE_COUNT;
        }
        glBindVertexArray(0);
        glUseProgram(0);
        //
        
        
        //render shadow volumes to the stencil buffer (z-fail method)
        glDepthFunc(GL_LESS);
        glStencilFunc(GL_ALWAYS, 0, 0xFF);
        {
            glDepthMask(false);
            glColorMask(false, false, false, false);
            {
                glCullFace(GL_FRONT);
                glStencilOp(GL_KEEP, GL_INCR, GL_KEEP);
                renderShadowVolumes();
            }
            {
                glCullFace(GL_BACK);
                glStencilOp(GL_KEEP, GL_DECR, GL_KEEP);
                renderShadowVolumes();
            }
            glColorMask(true, true, true, true);
            glDepthMask(true);
        }
        glStencilFunc(GL_EQUAL, 0, 0xFF);
        glDepthFunc(GL_LEQUAL);

        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
        //
        
        
        //render light again
        sun.getLightMode().enableAll();
        glUseProgram(Cube.SHADER_PROGRAM);
        CubeProgram.sendPerFrameUniforms(Cube.CUBE_TEXTURE, Cube.CUBE_TEXTURE_SPECULAR, cameraProjectionView, new Vector3f().set(camera.getPosition()), sun);

        glBindVertexArray(Cube.VAO);
        for (Cube c : cubes) {
            CubeProgram.sendPerDrawUniforms(c.getModel(), c.getNormalModel());
            glDrawElements(GL_TRIANGLES, Cube.CUBE_COUNT, GL_UNSIGNED_INT, Cube.CUBE_OFFSET);

            Main.NUMBER_OF_DRAWCALLS++;
            Main.NUMBER_OF_VERTICES += Cube.CUBE_COUNT;
        }
        glBindVertexArray(0);
        glUseProgram(0);
        
        glStencilFunc(GL_ALWAYS, 0, 0xFF);
        //
        
        
        //render shadow volumes
        if (this.showShadowVolumes) {
            glDepthFunc(GL_LESS);

            glUseProgram(CubeShadowVolumeProgram.SHADER_PROGRAM);
            CubeShadowVolumeProgram.sendPerFrameUniforms(cameraProjectionView, sun);
            glBindVertexArray(Cube.VAO);
            for (Cube c : cubes) {
                CubeShadowVolumeProgram.sendPerDrawUniforms(c.getModel(), c.getNormalModel());
                glDrawElements(GL_TRIANGLES, Cube.CUBE_SHADOW_VOLUME_COUNT, GL_UNSIGNED_INT, Cube.CUBE_SHADOW_VOLUME_OFFSET);

                Main.NUMBER_OF_DRAWCALLS++;
                Main.NUMBER_OF_VERTICES += Cube.CUBE_SHADOW_VOLUME_COUNT;
            }
            glBindVertexArray(0);
            glUseProgram(0);

            glDepthFunc(GL_LEQUAL);
        }
        //

        AabRender.renderQueue(camera);

        if (this.textEnabled) {
            GLFontRenderer.render(-1f, 0.90f,
                    new GLFontSpecification[]{
                        GLFontSpecifications.OPENSANS_ITALIC_0_10_BANANA_YELLOW,
                        GLFontSpecifications.ROBOTO_THIN_0_05_WHITE,},
                    new String[]{
                        "ShadowVolumeExperiment\n",
                        new StringBuilder()
                                .append("FPS: ").append(Main.FPS).append('\n')
                                .append("X: ").append(format(camera.getPosition().x())).append(" ")
                                .append("Y: ").append(format(camera.getPosition().y())).append(" ")
                                .append("Z: ").append(format(camera.getPosition().z())).append('\n')
                                .append("Controls:\n")
                                .append("\tWASD + Space + Mouse - Move\n")
                                .append("\tShift - Run\n")
                                .append("\tAlt - Wander\n")
                                .append("\tCtrl - Unlock/Lock mouse\n")
                                .append("\tF - Spawn Cube\n")
                                .append("\tR - Remove Last Cube").append(" [").append(this.cubes.size() - 1).append(" Cubes]\n")
                                .append("\tT - Hide This Wall of Text.\n")
                                .append("\tV - ").append((this.showShadowVolumes ? "Hide" : "Show")).append(" Shadow Volumes.\n")
                                .append("\tSpace - Change Light Direction.\n")
                                .toString()
                    }
            );
        }

        Main.WINDOW_TITLE += " (DrawCalls: " + Main.NUMBER_OF_DRAWCALLS + ", Vertices: " + Main.NUMBER_OF_VERTICES + ")";
        Main.WINDOW_TITLE += " (x:" + (int) Math.floor(camera.getPosition().x()) + ",y:" + (int) Math.floor(camera.getPosition().y()) + ",z:" + (int) Math.ceil(camera.getPosition().z()) + ")";
        if (!this.textEnabled) {
            Main.WINDOW_TITLE += " (T - Show Wall of Text)";
        }
    }

    private String format(double d) {
        return String.format("%.2f", d);
    }

    private String formatColor(float f) {
        return String.format("%.1f", f);
    }

    public void mouseCursorMoved(double x, double y) {
        camera.mouseCursorMoved(x, y);
    }

    public void windowSizeChanged(int width, int height) {
        camera.setDimensions(width, height);
    }

    public void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_F && action == GLFW_PRESS) {
            Vector3dc camPos = camera.getPosition();
            Matrix4f model = new Matrix4f()
                    .translate((float) camPos.x(), (float) camPos.y() - 0.6f, (float) camPos.z())
                    .rotateXYZ(
                            (float) (Math.random() * (Math.PI * 2.0)),
                            (float) (Math.random() * (Math.PI * 2.0)),
                            (float) (Math.random() * (Math.PI * 2.0))
                    )
                    .scale((float) (Math.random() * 2.5) + 0.5f);
            cubes.add(new Cube(model));
        }
        if (key == GLFW_KEY_R && action == GLFW_PRESS) {
            if (cubes.size() > 1) {
                cubes.remove(cubes.size() - 1);
            }
        }
        if (key == GLFW_KEY_T && action == GLFW_PRESS) {
            this.textEnabled = !this.textEnabled;
        }
        if (key == GLFW_KEY_V && action == GLFW_PRESS) {
            this.showShadowVolumes = !this.showShadowVolumes;
        }
    }

    public void mouseCallback(long window, int button, int action, int mods) {

    }
}
