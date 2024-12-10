package jade;


import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class LevelEditorScene extends Scene {

    private String vertexShaderSrc = "#version 330 core\n" +
            "layout(location=0) in vec3 aPos;\n" +
            "layout(location=1) in vec4 aColor;\n" +
            "\n" +
            "out vec4 fColor;\n" +
            "\n" +
            "void main() {\n" +
            "\n" +
            "    fColor = aColor;\n" +
            "    gl_Position = vec4(aPos, 1.0);\n" +
            "}";

    private String fragmentShaderSrc = "#version 330 core\n" +
            "\n" +
            "in vec4 fColor;\n" +
            "\n" +
            "out vec4 color;\n" +
            "\n" +
            "void main() {\n" +
            "    color = fColor;\n" +
            "}";

    private int vertexID, fragmentID, shaderProgram;

    private float[] vertexArray = {
            // possition            // color
            0.5f, -0.5f, 0.0f,      1.0f, 0.0f, 0.0f, 1.0f, // bottom right 0
            -0.5f, 0.5f, 0.0f,     0.0f, 1.0f, 0.0f, 1.0f, // top left     1
            0.5f, 0.5f, 0.0f,       1.0f, 0.0f, 1.0f, 1.0f, // top right    2
            -0.5f, -0.5f, 0.0f,     1.0f, 1.0f, 0.0f, 1.0f, // bottom left  3
    };

    // Must be in counter-clockwise order
    private int[] elementArray = {
            /*
                    X           X


                    X           X
             */
            2, 1, 0, // top right triangle
            0, 1, 3 // bottom left triangle
    };

    private int vaoID, vboID, eboID;

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        // Compile and link shaders

        // First load and compile vertex shader
        vertexID = glCreateShader(GL_VERTEX_SHADER);

        // Pass shader src to the GPU
        glShaderSource(vertexID, vertexShaderSrc);
        glCompileShader(vertexID);

        // Check for errors in compilation
        int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
            System.out.println("Error: 'defaultShader.glsl'\n\tVertex shader compilation failed");
            System.out.println(glGetShaderInfoLog(vertexID, len));
            assert  false : "";
        }

        // First load and compile vertex shader
        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);

        // Pass shader src to the GPU
        glShaderSource(fragmentID, fragmentShaderSrc);
        glCompileShader(fragmentID);

        // Check for errors in compilation
        success = glGetShaderi(fragmentID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
            System.out.println("Error: 'defaultShader.glsl'\n\tFragment shader compilation failed");
            System.out.println(glGetShaderInfoLog(fragmentID, len));
            assert  false : "";
        }

        // Link shaders and check for errors
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexID);
        glAttachShader(shaderProgram, fragmentID);
        glLinkProgram(shaderProgram);

        // Check for link errors
        success = glGetProgrami(shaderProgram, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int len = glGetProgrami(shaderProgram, GL_INFO_LOG_LENGTH);
            System.out.println("Error: 'defaultShader.glsl'\n\tLinking shaders failed");
            System.out.println(glGetProgramInfoLog(shaderProgram, len));
            System.out.println(glGetShaderInfoLog(fragmentID, len));
            assert  false : "";
        }

        // Generate VAO, VBO, and EBO buffer objects and send to GPU
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Create a float buffer of vertices
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();

        // Create the vbo and upload vertex buffer
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // Create indices
//        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
//        elementBuffer.put(elementArray).flip();
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();

        eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

        // Add vertex attribute buffers
        int positionsSize = 3;
        int colorsSize = 4;
        int floatSizeBytes = 4;
        int vertexSizeBytes = (positionsSize + colorsSize) * floatSizeBytes;
        glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, colorsSize, GL_FLOAT, false, vertexSizeBytes, positionsSize * floatSizeBytes);
        glEnableVertexAttribArray(1);
    }

    @Override
    public void update(float dt) {
        // Bind shader program
        glUseProgram(shaderProgram);
        // Bind vao
        glBindVertexArray(vaoID);
        // Enable vertex attribute pointers
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);

        // Unbind everything
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        glBindVertexArray(0);

        glUseProgram(0);
    }
}
