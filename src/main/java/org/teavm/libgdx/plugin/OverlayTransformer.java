package org.teavm.libgdx.plugin;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.BufferUtils;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.teavm.diagnostics.Diagnostics;
import org.teavm.libgdx.emu.BufferUtilsEmulator;
import org.teavm.libgdx.emu.TextureDataEmulator;
import org.teavm.model.*;
import org.teavm.model.instructions.*;
import org.teavm.model.util.ModelUtils;

public class OverlayTransformer implements ClassHolderTransformer {
    @Override
    public void transformClass(ClassHolder cls, ClassReaderSource innerSource, Diagnostics diagnostics) {
        if (cls.getName().equals(BufferUtils.class.getName())) {
            transformBufferUtils(cls, innerSource);
        } else if (cls.getName().equals(TextureData.Factory.class.getName())) {
            transformTextureData(cls, innerSource);
        } else if (cls.getName().equals(FileHandle.class.getName())) {
            transformFileHandle(cls);
        }
    }

    private void transformBufferUtils(ClassHolder cls, ClassReaderSource innerSource) {
        List<MethodDescriptor> descList = new ArrayList<>();
        descList.add(new MethodDescriptor("freeMemory", ByteBuffer.class, void.class));
        descList.add(new MethodDescriptor("newDisposableByteBuffer", int.class, ByteBuffer.class));
        replaceMethods(cls, BufferUtilsEmulator.class, innerSource, descList);
    }

    private void transformTextureData(ClassHolder cls, ClassReaderSource innerSource) {
        List<MethodDescriptor> descList = new ArrayList<>();
        descList.add(new MethodDescriptor("loadFromFile", FileHandle.class, Format.class, boolean.class,
                TextureData.class));
        replaceMethods(cls, TextureDataEmulator.class, innerSource, descList);
    }

    private void transformFileHandle(ClassHolder cls) {
        Set<MethodDescriptor> methodsToRetain = new HashSet<>();
        Set<MethodDescriptor> methodsToRetainUnmodified = new HashSet<>();
        methodsToRetain.add(new MethodDescriptor("<init>", void.class));
        methodsToRetain.add(new MethodDescriptor("<init>", String.class, void.class));
        methodsToRetain.add(new MethodDescriptor("<init>", String.class, FileType.class, void.class));
        methodsToRetain.add(new MethodDescriptor("path", String.class));
        methodsToRetain.add(new MethodDescriptor("name", String.class));
        methodsToRetain.add(new MethodDescriptor("extension", String.class));
        methodsToRetain.add(new MethodDescriptor("nameWithoutExtension", String.class));
        methodsToRetain.add(new MethodDescriptor("pathWithoutExtension", String.class));
        methodsToRetain.add(new MethodDescriptor("type", FileType.class));
        methodsToRetain.add(new MethodDescriptor("read", InputStream.class));
        methodsToRetainUnmodified.add(new MethodDescriptor("read", int.class, BufferedInputStream.class));
        methodsToRetainUnmodified.add(new MethodDescriptor("reader", Reader.class));
        methodsToRetainUnmodified.add(new MethodDescriptor("reader", int.class, BufferedReader.class));
        methodsToRetain.add(new MethodDescriptor("readString", String.class));
        methodsToRetain.add(new MethodDescriptor("readBytes", byte[].class));
        methodsToRetain.add(new MethodDescriptor("estimateLength", int.class));
        methodsToRetain.add(new MethodDescriptor("readBytes", int[].class, int.class, int.class, int.class));
        methodsToRetain.add(new MethodDescriptor("list", FileHandle[].class));
        methodsToRetain.add(new MethodDescriptor("list", String.class, FileHandle[].class));
        methodsToRetain.add(new MethodDescriptor("isDirectory", boolean.class));
        methodsToRetain.add(new MethodDescriptor("child", String.class, FileHandle.class));
        methodsToRetain.add(new MethodDescriptor("sibling", String.class, FileHandle.class));
        methodsToRetain.add(new MethodDescriptor("parent", FileHandle.class));
        methodsToRetain.add(new MethodDescriptor("exists", boolean.class));
        methodsToRetain.add(new MethodDescriptor("length", long.class));
        methodsToRetain.add(new MethodDescriptor("lastModified", long.class));
        methodsToRetainUnmodified.add(new MethodDescriptor("equals", Object.class, boolean.class));
        methodsToRetainUnmodified.add(new MethodDescriptor("hashCode", int.class));
        for (MethodHolder method : cls.getMethods().toArray(new MethodHolder[0])) {
            if (methodsToRetain.contains(method.getDescriptor())) {
                if (method.getName().equals("<init>")) {
                    method.setProgram(createInitStubProgram());
                } else {
                    method.setProgram(createStubProgram());
                }
            } else if (!methodsToRetainUnmodified.contains(method.getDescriptor())) {
                cls.removeMethod(method);
            }
        }
        for (FieldHolder field : cls.getFields().toArray(new FieldHolder[0])) {
            cls.removeField(field);
        }
    }

    private Program createStubProgram() {
        Program program = new Program();
        program.createVariable(); // this
        BasicBlock block = program.createBasicBlock();
        Variable ex = program.createVariable();
        ConstructInstruction consInsn = new ConstructInstruction();
        consInsn.setReceiver(ex);
        consInsn.setType(UnsupportedOperationException.class.getName());
        block.getInstructions().add(consInsn);
        InvokeInstruction initInsn = new InvokeInstruction();
        initInsn.setType(InvocationType.SPECIAL);
        initInsn.setInstance(ex);
        initInsn.setMethod(new MethodReference(UnsupportedOperationException.class, "<init>", void.class));
        block.getInstructions().add(initInsn);
        RaiseInstruction raiseInsn = new RaiseInstruction();
        raiseInsn.setException(ex);
        block.getInstructions().add(raiseInsn);
        return program;
    }

    private Program createInitStubProgram() {
        Program program = new Program();
        BasicBlock block = program.createBasicBlock();
        Variable self = program.createVariable();
        InvokeInstruction superInitInsn = new InvokeInstruction();
        superInitInsn.setType(InvocationType.SPECIAL);
        superInitInsn.setInstance(self);
        superInitInsn.setMethod(new MethodReference(Object.class, "<init>", void.class));
        block.getInstructions().add(superInitInsn);
        ExitInstruction exitInsn = new ExitInstruction();
        block.getInstructions().add(exitInsn);
        return program;
    }

    private void replaceMethods(ClassHolder cls, Class<?> emuType, ClassReaderSource innerSource,
            List<MethodDescriptor> descList) {
        ClassReader emuCls = innerSource.get(emuType.getName());
        for (MethodDescriptor methodDesc : descList) {
            cls.removeMethod(cls.getMethod(methodDesc));
            cls.addMethod(ModelUtils.copyMethod(emuCls.getMethod(methodDesc)));
        }
    }
}
