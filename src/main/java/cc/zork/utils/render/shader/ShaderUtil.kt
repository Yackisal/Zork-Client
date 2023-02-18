package cc.zork.utils.render.shader

import cc.zork.utils.os.FileUtils.readInputStream
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

class ShaderUtil @JvmOverloads constructor(
    fragmentShaderLoc: String?,
    vertexShaderLoc: String? = "client/shaders/vertex.vsh"
) {
    private val programID: Int
    fun init() {
        GL20.glUseProgram(programID)
    }

    fun unload() {
        GL20.glUseProgram(0)
    }

    fun getUniform(name: String?): Int {
        return GL20.glGetUniformLocation(programID, name)
    }

    fun setUniformf(name: String?, vararg args: Float) {
        val loc = GL20.glGetUniformLocation(programID, name)
        when (args.size) {
            1 -> GL20.glUniform1f(loc, args[0])
            2 -> GL20.glUniform2f(loc, args[0], args[1])
            3 -> GL20.glUniform3f(loc, args[0], args[1], args[2])
            4 -> GL20.glUniform4f(loc, args[0], args[1], args[2], args[3])
        }
    }

    fun setUniformi(name: String?, vararg args: Int) {
        val loc = GL20.glGetUniformLocation(programID, name)
        if (args.size > 1) GL20.glUniform2i(loc, args[0], args[1]) else GL20.glUniform1i(loc, args[0])
    }

    private fun createShader(inputStream: InputStream, shaderType: Int): Int {
        val shader = GL20.glCreateShader(shaderType)
        GL20.glShaderSource(shader, readInputStream(inputStream))
        GL20.glCompileShader(shader)
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) {
            println(GL20.glGetShaderInfoLog(shader, 4096))
            throw IllegalStateException(String.format("Shader (%s) failed to compile!", shaderType))
        }
        return shader
    }

    private val roundedRectGradient = "#version 120\n" +
            "\n" +
            "uniform vec2 location, rectSize;\n" +
            "uniform vec4 color1, color2, color3, color4;\n" +
            "uniform float radius;\n" +
            "\n" +
            "#define NOISE .5/255.0\n" +
            "\n" +
            "float roundSDF(vec2 p, vec2 b, float r) {\n" +
            "    return length(max(abs(p) - b , 0.0)) - r;\n" +
            "}\n" +
            "\n" +
            "vec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4){\n" +
            "    vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);\n" +
            "    //Dithering the color\n" +
            "    // from https://shader-tutorial.dev/advanced/color-banding-dithering/\n" +
            "    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));\n" +
            "    return color;\n" +
            "}\n" +
            "\n" +
            "void main() {\n" +
            "    vec2 st = gl_TexCoord[0].st;\n" +
            "    vec2 halfSize = rectSize * .5;\n" +
            "    \n" +
            "    float smoothedAlpha =  (1.0-smoothstep(0.0, 2., roundSDF(halfSize - (gl_TexCoord[0].st * rectSize), halfSize - radius - 1., radius))) * color1.a;\n" +
            "    gl_FragColor = vec4(createGradient(st, color1.rgb, color2.rgb, color3.rgb, color4.rgb), smoothedAlpha);\n" +
            "}"
    private val roundedRect = """#version 120

uniform vec2 location, rectSize;
uniform vec4 color;
uniform float radius;
uniform bool blur;

float roundSDF(vec2 p, vec2 b, float r) {
    return length(max(abs(p) - b, 0.0)) - r;
}


void main() {
    vec2 rectHalf = rectSize * .5;
    // Smooth the result (free antialiasing).
    float smoothedAlpha =  (1.0-smoothstep(0.0, 1.0, roundSDF(rectHalf - (gl_TexCoord[0].st * rectSize), rectHalf - radius - 1., radius))) * color.a;
    gl_FragColor = vec4(color.rgb, smoothedAlpha);// mix(quadColor, shadowColor, 0.0);

}"""

    init {
        val program = GL20.glCreateProgram()
        try {
            val fragmentShaderID: Int
            fragmentShaderID = when (fragmentShaderLoc) {
                "roundedRect" -> createShader(ByteArrayInputStream(roundedRect.toByteArray()), GL20.GL_FRAGMENT_SHADER)
                "roundedRectGradient" -> createShader(
                    ByteArrayInputStream(roundedRectGradient.toByteArray()),
                    GL20.GL_FRAGMENT_SHADER
                )

                else -> createShader(
                    Minecraft.getMinecraft().resourceManager.getResource(
                        ResourceLocation(
                            fragmentShaderLoc
                        )
                    ).inputStream, GL20.GL_FRAGMENT_SHADER
                )
            }
            GL20.glAttachShader(program, fragmentShaderID)
            val vertexShaderID = createShader(
                Minecraft.getMinecraft().resourceManager.getResource(ResourceLocation(vertexShaderLoc)).inputStream,
                GL20.GL_VERTEX_SHADER
            )
            GL20.glAttachShader(program, vertexShaderID)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        GL20.glLinkProgram(program)
        val status = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS)
        check(status != 0) { "Shader failed to link!" }
        programID = program
    }

    companion object {
        fun drawQuads(x: Float, y: Float, width: Float, height: Float) {
            if (Minecraft.getMinecraft().gameSettings.ofFastRender) return
            GL11.glBegin(GL11.GL_QUADS)
            GL11.glTexCoord2f(0f, 0f)
            GL11.glVertex2f(x, y)
            GL11.glTexCoord2f(0f, 1f)
            GL11.glVertex2f(x, y + height)
            GL11.glTexCoord2f(1f, 1f)
            GL11.glVertex2f(x + width, y + height)
            GL11.glTexCoord2f(1f, 0f)
            GL11.glVertex2f(x + width, y)
            GL11.glEnd()
        }

        fun drawQuads() {
            if (Minecraft.getMinecraft().gameSettings.ofFastRender) return
            val sr = ScaledResolution(Minecraft.getMinecraft())
            val width = sr.scaledWidth_double.toFloat()
            val height = sr.scaledHeight_double.toFloat()
            GL11.glBegin(GL11.GL_QUADS)
            GL11.glTexCoord2f(0f, 1f)
            GL11.glVertex2f(0f, 0f)
            GL11.glTexCoord2f(0f, 0f)
            GL11.glVertex2f(0f, height)
            GL11.glTexCoord2f(1f, 0f)
            GL11.glVertex2f(width, height)
            GL11.glTexCoord2f(1f, 1f)
            GL11.glVertex2f(width, 0f)
            GL11.glEnd()
        }
    }
}