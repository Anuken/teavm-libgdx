package org.teavm.libgdx.plugin;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.*;

import org.reflections.Reflections;
import org.teavm.diagnostics.Diagnostics;
import org.teavm.libgdx.emu.*;
import org.teavm.libgdx.plugin.Annotations.Emulate;
import org.teavm.libgdx.plugin.Annotations.Replace;
import org.teavm.model.*;
import org.teavm.model.instructions.*;
import org.teavm.model.util.ModelUtils;
import org.teavm.parsing.ClassRefsRenamer;

public class OverlayTransformer implements ClassHolderTransformer {
    private HashMap<String, Class<?>> emulations = new HashMap<>();
    private HashMap<String, Class<?>> replacements = new HashMap<>();

    public OverlayTransformer(){
        Reflections reflections = new Reflections("org.teavm.libgdx.emu");

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Emulate.class);
        for(Class<?> type : annotated){
            Emulate em = type.getAnnotation(Emulate.class);
            Class<?> toEmulate = em.value();
            emulations.put(toEmulate.getTypeName(), type);
        }

        annotated = reflections.getTypesAnnotatedWith(Replace.class);
        for(Class<?> type : annotated){
            Replace em = type.getAnnotation(Replace.class);
            Class<?> toEmulate = em.value();
            replacements.put(toEmulate.getTypeName(), type);
        }
    }

    @Override
    public void transformClass(ClassHolder cls, ClassReaderSource innerSource, Diagnostics diagnostics) {
        try{
            if(emulations.containsKey(cls.getName())){
                System.out.println("Emulating " + cls.getName());
                Class<?> emulated = emulations.get(cls.getName());
                List<MethodDescriptor> descList = new ArrayList<>();
                for(Method method : emulated.getDeclaredMethods()){
                    Class[] classes = new Class[method.getParameterTypes().length + 1];
                    classes[classes.length-1] = method.getReturnType();
                    System.arraycopy(method.getParameterTypes(), 0, classes, 0, method.getParameterTypes().length);
                    descList.add(new MethodDescriptor(method.getName(), classes));
                }
                replaceMethods(cls, emulated, innerSource, descList);
            }else if(replacements.containsKey(cls.getName())){
                System.out.println("Replacing " + cls.getName());
                Class<?> emulated = replacements.get(cls.getName());
                replaceClass(cls, innerSource.get(emulated.getName()));
            }
        }catch(Exception e){
            throw new RuntimeException("Error occurred transforming " + cls.getName(), e);
        }
    }

    private void replaceMethods(ClassHolder cls, Class<?> emuType, ClassReaderSource innerSource,
            List<MethodDescriptor> descList) {
        ClassReader emuCls = innerSource.get(emuType.getName());
        for (MethodDescriptor methodDesc : descList) {
            if(cls.getMethod(methodDesc) != null){
                cls.removeMethod(cls.getMethod(methodDesc));
            }
            cls.addMethod(ModelUtils.copyMethod(emuCls.getMethod(methodDesc)));
        }
    }

    private void replaceClass(final ClassHolder cls, final ClassReader emuCls) {
        ClassRefsRenamer renamer = new ClassRefsRenamer(preimage ->
                preimage.equals(emuCls.getName()) ? cls.getName() : preimage);
        for (FieldHolder field : cls.getFields().toArray(new FieldHolder[0])) {
            cls.removeField(field);
        }
        for (MethodHolder method : cls.getMethods().toArray(new MethodHolder[0])) {
            cls.removeMethod(method);
        }
        for (FieldReader field : emuCls.getFields()) {
            cls.addField(ModelUtils.copyField(field));
        }
        for (MethodReader method : emuCls.getMethods()) {
            cls.addMethod(renamer.rename(ModelUtils.copyMethod(method)));
        }
    }
}
