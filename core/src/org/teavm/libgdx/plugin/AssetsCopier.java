/*
 *  Copyright 2015 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.libgdx.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.teavm.backend.javascript.rendering.RenderingManager;
import org.teavm.vm.BuildTarget;
import org.teavm.vm.spi.RendererListener;

/**
 * @author Alexey Andreev
 */
public class AssetsCopier implements RendererListener {
    private RenderingManager context;
    private FileDescriptor rootFileDescriptor = new FileDescriptor();
    private ObjectMapper mapper = new ObjectMapper();
    private ObjectWriter writer = mapper.writerFor(FileDescriptor.class);
    private String[] classpathAssets = {
        "com/badlogic/gdx/graphics/g3d/particles/particles.fragment.glsl",
        "com/badlogic/gdx/graphics/g3d/particles/particles.vertex.glsl",
        "com/badlogic/gdx/graphics/g3d/shaders/default.fragment.glsl",
        "com/badlogic/gdx/graphics/g3d/shaders/default.vertex.glsl",
        "com/badlogic/gdx/graphics/g3d/shaders/depth.fragment.glsl",
        "com/badlogic/gdx/graphics/g3d/shaders/depth.vertex.glsl",
        "com/badlogic/gdx/utils/arial-15.fnt",
        "com/badlogic/gdx/utils/arial-15.png"
    };

    @Override
    public void begin(RenderingManager context, BuildTarget buildTarget){
        this.context = context;
    }

    @Override
    public void complete() throws IOException {
        String dirName = context.getProperties().getProperty("teavm.libgdx.genAssetsDirectory", "html/webapp/assets");
        if (!dirName.isEmpty()) {
            File dir = new File(dirName);
            dir.mkdirs();
            copyClasspathAssets(dir);
            copyAssets();
            createFSDescriptor(dir);
        } else {
            createFSDescriptor(null);
        }
    }

    private void copyAssets() throws IOException{
        Path path = Paths.get("core/assets/");

        Files.walk(path).forEach(sr -> {
            Path dest = Paths.get("html/webapp/assets/").resolve(path.relativize(sr));
            if(Files.isDirectory(sr) || Files.isDirectory(dest)) return;
            System.out.println("Copy " + sr + " -> " + dest);
            try{
                if(!Files.exists(dest)) Files.createDirectories(dest);
                Files.copy(sr, dest, StandardCopyOption.REPLACE_EXISTING);
            }catch(IOException e){
                throw new RuntimeException(e);
            }
        });
    }

    private void copyClasspathAssets(File dir) throws IOException {
        Set<String> resourcesToCopy = new HashSet<>(Arrays.asList(classpathAssets));

        for (String resourceToCopy : resourcesToCopy) {
            File resource = new File(dir, resourceToCopy);
            if (resource.exists()) {
                URL url = context.getClassLoader().getResource(resourceToCopy);
                if (url != null && url.getProtocol().equals("file")) {
                    try {
                        File sourceFile = new File(url.toURI());
                        if (sourceFile.exists() && sourceFile.length() == resource.length() &&
                                sourceFile.lastModified() == resource.lastModified()) {
                            continue;
                        }
                    } catch (URISyntaxException e) {
                        // fall back to usual resource copying
                    }
                }
            }
            InputStream input = context.getClassLoader().getResourceAsStream(resourceToCopy);
            if (input == null) {
                continue;
            }
            resource.getParentFile().mkdirs();
            IOUtils.copy(input, new FileOutputStream(resource));
        }
    }

    private void createFSDescriptor(File dir) throws IOException {
        System.out.println("Creating filesystem...");
        String path = context.getProperties().getProperty("teavm.libgdx.fsJsonPath", "html/webapp/filesystem.json");
        if (path.isEmpty()) {
            return;
        }
        if (dir != null) {
            processFile(dir, rootFileDescriptor);
        }

        String dirName = context.getProperties().getProperty("teavm.libgdx.warAssetsDirectory", "core/assets/");
        if (!dirName.isEmpty()) {
            dir = new File(dirName);
            processFile(dir, rootFileDescriptor);
        }

        try (FileOutputStream output = new FileOutputStream(new File(path))) {
            writeJsonFS(output);
        }
    }

    private void processFile(File file, FileDescriptor desc) {
        desc.setName(file.getName());
        desc.setDirectory(file.isDirectory());
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                System.out.println("Copying " + child);
                FileDescriptor childDesc = new  FileDescriptor();
                processFile(child, childDesc);
                desc.getChildFiles().add(childDesc);
            }
        }
    }

    private void writeJsonFS(OutputStream output) throws IOException {
        SequenceWriter seqWriter = writer.writeValues(output);
        boolean first = true;
        output.write((byte)'[');
        for (FileDescriptor desc : rootFileDescriptor.getChildFiles()) {
            if (!first) {
                output.write((byte)',');
            }
            first = false;
            seqWriter.write(desc);
        }
        output.write((byte)']');
        seqWriter.flush();
    }
}
