
/*
 * Janino - An embedded Java[TM] compiler
 *
 * Copyright (c) 2001-2010, Arno Unkrig
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *       following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *       following disclaimer in the documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products derived from this software without
 *       specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.praxislive.compiler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.JavaFileObject.Kind;

public
class JavaFileManagerClassLoader extends ClassLoader {

    private final JavaFileManager javaFileManager;

    public
    JavaFileManagerClassLoader(JavaFileManager javaFileManager) { this.javaFileManager = javaFileManager; }

    public
    JavaFileManagerClassLoader(JavaFileManager javaFileManager, ClassLoader parentClassLoader) {
        super(parentClassLoader);
        this.javaFileManager = javaFileManager;
    }

    @Override protected Class<?>
    findClass(String className) throws ClassNotFoundException {
        byte[] ba;
        try {
            JavaFileObject classFile = this.javaFileManager.getJavaFileForInput(
                StandardLocation.CLASS_OUTPUT,
                className,
                Kind.CLASS
            );
            if (classFile == null) throw new ClassNotFoundException(className);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            {
                InputStream is = classFile.openInputStream();
                try {
                    byte[] buffer = new byte[8192];
                    for (;;) {
                        int count = is.read(buffer);
                        if (count == -1) break;
                        baos.write(buffer, 0, count);
                    }
                } finally {
                    try { is.close(); } catch (Exception e) {}
                }
            }
            ba = baos.toByteArray();
        } catch (IOException ioe) {
            throw new ClassNotFoundException(className, ioe);
        }
        return this.defineClass(className, ba, 0, ba.length);
    }
}
