package org.solhost.folko.uosl.slclient.views;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// Inspired by http://goharsha.com/lwjgl-tutorial-series/encapsulating-shaders/

public class ShaderProgram {
    private Integer programId;
    private Integer vertexShaderId;
    private Integer fragmentShaderId;

    public ShaderProgram() {
        programId = glCreateProgram();
    }

    public void setVertexShader(Path path) throws IOException {
        vertexShaderId = readShader(path, GL_VERTEX_SHADER);
    }

    public void setFragmentShader(Path path) throws IOException {
        fragmentShaderId = readShader(path, GL_FRAGMENT_SHADER);
    }

    public int getUniformLocation(String name) {
        int location = glGetUniformLocation(programId, name);
        if(location == -1) {
            throw new RuntimeException("Invalid uniform: " + location);
        }
        return location;
    }

    public void setUniformInt(int location, int value) {
        if(location == -1) {
            throw new RuntimeException("Invalid uniform: " + location);
        }
        glUniform1i(location, value);
    }

    public void setUniformFloat(int location, float... values) {
        if(location == -1) {
            throw new RuntimeException("Invalid uniform: " + location);
        }

        switch(values.length) {
            case 1:
                glUniform1f(location, values[0]);
                break;
            case 2:
                glUniform2f(location, values[0], values[1]);
                break;
            case 3:
                glUniform3f(location, values[0], values[1], values[2]);
                break;
            case 4:
                glUniform4f(location, values[0], values[1], values[2], values[3]);
                break;
            default:
                throw new RuntimeException("Invalid uniform size: " + values.length);
        }
    }

    public void setUniformBool(int location, boolean val) {
        if(location == -1) {
            throw new RuntimeException("Invalid uniform: " + location);
        }
        glUniform1i(location, val ? GL_TRUE : GL_FALSE);
    }

    public void setUniformMatrix(int location, Transform t) {
        if(location == -1) {
            throw new RuntimeException("Invalid uniform: " + location);
        }
        glUniformMatrix4(location, false, t.getFloatBuffer());
    }

    public void link() {
        glLinkProgram(programId);
        if(glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            String error = glGetProgramInfoLog(programId, glGetProgrami(programId, GL_INFO_LOG_LENGTH));
            throw new RuntimeException("Couldn't link shader: " + error);
        }
        if(vertexShaderId != null) {
            glDetachShader(programId, vertexShaderId);
            glDeleteShader(vertexShaderId);
            vertexShaderId = null;
        }

        if(fragmentShaderId != null) {
            glDetachShader(programId, fragmentShaderId);
            glDeleteShader(fragmentShaderId);
            fragmentShaderId = null;
        }
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void dispose() {
        unbind();

        if(programId == null) {
            return;
        }

        if(vertexShaderId != null) {
            glDetachShader(programId, vertexShaderId);
            glDeleteShader(vertexShaderId);
            vertexShaderId = null;
        }

        if(fragmentShaderId != null) {
            glDetachShader(programId, fragmentShaderId);
            glDeleteShader(fragmentShaderId);
            fragmentShaderId = null;
        }

        glDeleteProgram(programId);
        programId = null;
    }

    private int readShader(Path path, int type) throws IOException {
        byte[] fileContents = Files.readAllBytes(path);
        String shaderSource = new String(fileContents, StandardCharsets.UTF_8);
        int shaderId = glCreateShader(type);
        glShaderSource(shaderId, shaderSource);
        glCompileShader(shaderId);
        if(glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            String error = glGetShaderInfoLog(shaderId, glGetShaderi(shaderId, GL_INFO_LOG_LENGTH));
            throw new RuntimeException("Couldn't compile shader: " + error);
        }
        glAttachShader(programId, shaderId);
        return shaderId;
    }
}
