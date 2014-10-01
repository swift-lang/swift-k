/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 20, 2013
 */
package org.griphyn.vdl.mapping.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingParamFileGenerator {
    public static void main(String[] args) {
        try {
            generate(args[0]);
            System.exit(0);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void generate(String basedir) throws IOException {
        File bd = new File(basedir);
        if (!bd.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + basedir);
        }
        generateRecursive(bd, bd);
    }
    
    private static void generateRecursive(File dir, File base) throws IOException {
        File[] ps = dir.listFiles();
        for (File f : ps) {
            if (f.isDirectory()) {
                generateRecursive(f, base);
            }
            else if (f.getName().endsWith(".params")) {
                generate(f, base);
            }
        }
    }
    
    private static class Param {
        public String type, name, value;
        public boolean internal;
        
        public Param(String type, String name, String value, boolean internal) {
            this.type = type;
            this.name = name;
            this.value = value;
            this.internal = internal;
        }
    }

    private static void generate(File f, File base) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        List<Param> params = new ArrayList<Param>();
        String line = br.readLine();
        Map<String, Object> opts = new HashMap<String, Object>();
        List<String> imports = new ArrayList<String>();
        opts.put("import", imports);
        while (line != null) {
            if (line.startsWith("#")) {
                // comment
            }
            else if (line.trim().isEmpty()) {
                // empty line
            }
            else if (line.startsWith("@")) {
                String[] s = line.substring(1).trim().split("\\s+", 2);
                if (s[0].equals("import")) {
                    imports.add(s[1]);
                }
                else if (s[0].equals("extends")) {
                    opts.put("extends", s[1]);
                }
                else if (s[0].equals("internal")) {
                    params.add(buildParam(s[1], true));
                }
                else if (s[0].equals("suppressUnusedWarning")) {
                    opts.put("suppressUnusedWarning", Boolean.TRUE);
                }
                else {
                    throw new IllegalArgumentException("Unknown directive: '" + s[0] + "'");
                }
            }
            else {
                params.add(buildParam(line, false));
            }
            line = br.readLine();
        }
        br.close();
        
        String pkg = f.getParentFile().getAbsolutePath().substring(
            base.getAbsolutePath().length() + 1).replace(File.separatorChar, '.');
        File nf = new File(makeFileName(f));
        writeFile(nf, pkg, params, opts);
    }
    
    private static Param buildParam(String line, boolean internal) {
        String value = null;
        if (line.contains("=")) {
            String[] s = line.trim().split("=", 2);
            value = s[1].trim();
            line = s[0];
        }
        String[] s = line.trim().split("\\s+");
        return new Param(join(s, " ", 0, s.length - 1), s[s.length - 1], value, internal);
    }

    private static String join(String[] s, String sep, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            if (i != start) {
                sb.append(sep);
            }
            sb.append(s[i]);
        }
        return sb.toString();
    }

    private static final List<String> IMPORTS = Arrays.asList("java.util.Arrays", "java.util.Collection", 
        "java.util.List", "org.griphyn.vdl.mapping.nodes.AbstractDataNode", "org.griphyn.vdl.mapping.MappingParamSet");

    private static void writeFile(File nf, String pkg, List<Param> params, Map<String, Object> opts) throws IOException {
        String name = nf.getName().substring(0, nf.getName().lastIndexOf('.'));
        BufferedWriter bw = new BufferedWriter(new FileWriter(nf));

        int year = Calendar.getInstance().get(Calendar.YEAR);

        bw.write("/*\n");
        bw.write(" * Swift Parallel Scripting Language (http://swift-lang.org)\n");
        bw.write(" *\n");
        bw.write(" * Copyright 2013-" + year + " University of Chicago\n");
        bw.write(" *\n");
        bw.write(" * Licensed under the Apache License, Version 2.0 (the \"License\");\n");
        bw.write(" * you may not use this file except in compliance with the License.\n");
        bw.write(" * You may obtain a copy of the License at\n");
        bw.write(" *\n");
        bw.write(" *  http://www.apache.org/licenses/LICENSE-2.0\n");
        bw.write(" *\n");
        bw.write(" * Unless required by applicable law or agreed to in writing, software\n");
        bw.write(" * distributed under the License is distributed on an \"AS IS\" BASIS,\n");
        bw.write(" * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n");
        bw.write(" * See the License for the specific language governing permissions and\n");
        bw.write(" * limitations under the License.\n");
        bw.write(" */\n");
        bw.write("\n");
        bw.write("/*\n");
        bw.write(" * This file is automatically generated\n");
        bw.write(" */\n");


        bw.write("package ");
        bw.write(pkg);
        bw.write(";\n\n");
        
        for (String imp : IMPORTS) {
            bw.write("import ");
            bw.write(imp);
            bw.write(";\n");
        }
        @SuppressWarnings("unchecked")
        List<String> l = (List<String>) opts.get("import");
        for (String imp : l) {
            bw.write("import ");
            bw.write(imp);
            bw.write(";\n");
        }
        
        
        if (!opts.containsKey("extends")) {
            opts.put("extends", "MappingParamSet");
        }
        
        bw.write("\n\n");
        if (opts.containsKey("suppressUnusedWarning")) {
            bw.write("@SuppressWarnings(\"unused\")\n");
        }
        bw.write("public class ");
        bw.write(name);
        bw.write(" extends ");
        bw.write(opts.get("extends").toString());
        bw.write(" {\n\n");
        
        bw.write("\tpublic static final List<String> NAMES = Arrays.asList(");
        join(bw, params, "\"", "\", \"", "\"", false);
        bw.write(");\n\n");
        
        for (Param p : params) {
            bw.write("\tprivate ");
            if (p.internal) {
                bw.write(p.type);
            }
            else {
                bw.write("Object");
            }
            bw.write(" ");
            bw.write(p.name);
            if (p.value != null) {
                bw.write(" = ");
                bw.write(p.value);
            }
            bw.write(";\n");
        }
        bw.write("\n");
        
        bw.write("\t@Override\n");
        bw.write("\tpublic Collection<String> getNames() {\n");
        bw.write("\t\treturn NAMES;\n");
        bw.write("\t}\n\n");
        
        generateGettersAndSetters(bw, params);
        
        if (!allInternal(params)) {
            generateDynamicSetter(bw, params);
            generateGetFirstOpen(bw, params);
            
            generateToString(bw, params);
            generateUnwrap(bw, params);
        }
        
        bw.write("\n}\n");
        
        bw.close();
    }

    private static boolean allInternal(List<Param> params) {
        int c = 0;
        for (Param p : params) {
            if (!p.internal) {
                c++;
            }
        }
        return c == 0;
    }

    private static void generateToString(BufferedWriter bw, List<Param> params) throws IOException {
        bw.write("\t@Override\n");
        bw.write("\tpublic void toString(StringBuilder sb) {\n");
        for (Param p : params) {
            if (!p.internal) {
                bw.write(String.format("\t\taddParam(sb, \"%s\", %s);\n", p.name, p.name));
            }
        }
        bw.write("\t\tsuper.toString(sb);\n");
        bw.write("\t}\n\n");
    }
    
    private static void generateUnwrap(BufferedWriter bw, List<Param> params) throws IOException {
        bw.write("\t@Override\n");
        bw.write("\tpublic void unwrapPrimitives() {\n");
        for (Param p : params) {
            if (!p.internal) {
                if (p.value == null) {
                    // mandatory
                    bw.write(String.format("\t\tif (%s == null) {\n", p.name));
                    bw.write(String.format("\t\t\tthrow new IllegalArgumentException(\"Missing required argument '%s'\");\n", p.name));
                    bw.write("\t\t}\n");
                }
                if (!p.type.equals("DSHandle")) {
                    bw.write(String.format("\t\t%s = unwrap(%s, %s.class);\n", p.name, p.name, p.type));
                }
            }
        }
        bw.write("\t\tsuper.unwrapPrimitives();\n");
        bw.write("\t}\n\n");
    }

    private static void generateGettersAndSetters(BufferedWriter bw, List<Param> params) throws IOException {
        for (Param p : params) {
            bw.write(String.format("\tpublic void set%s(%s %s) {\n", capitalize(p.name), p.type, p.name));
            bw.write(String.format("\t\tthis.%s = %s;\n", p.name, p.name));
            bw.write("\t}\n\n");
            
            bw.write(String.format("\tpublic %s get%s() {\n", p.type, capitalize(p.name)));
            if (p.type.equals("Object") || p.internal) {
                bw.write(String.format("\t\treturn %s;\n", p.name));
            }
            else {
                bw.write(String.format("\t\treturn (%s) %s;\n", p.type, p.name));
            }
            bw.write("\t}\n\n");
        }
    }
    
    private static void generateDynamicSetter(BufferedWriter bw, List<Param> params) throws IOException {
        bw.write("\t@Override\n");
        bw.write("\tprotected boolean set0(String name, Object value) {\n");
        boolean first = true;
        for (Param p : params) {
            if (p.internal) {
                continue;
            }
            bw.write("\t\t");
            if (first) {
                first = false;
            }
            else {
                bw.write("else ");
            }
            bw.write(String.format("if (name.equals(\"%s\")) {\n", p.name));
            bw.write(String.format("\t\t\tthis.%s = value;\n", p.name));
            bw.write("\t\t}\n");
        }
        bw.write("\t\telse {\n");
        bw.write("\t\t\treturn super.set0(name, value);\n");
        bw.write("\t\t}\n");
        bw.write("\t\treturn true;\n");
        bw.write("\t}\n\n");
    }
    
    private static void generateGetFirstOpen(BufferedWriter bw, List<Param> params) throws IOException {
        bw.write("\t@Override\n");
        bw.write("\tpublic AbstractDataNode getFirstOpen() {\n");
        boolean first = true;
        for (Param p : params) {
            if (p.internal) {
                continue;
            }
            bw.write("\t\t");
            if (first) {
                first = false;
            }
            else {
                bw.write("else ");
            }
            bw.write(String.format("if (checkOpen(%s)) {\n", p.name));
            bw.write(String.format("\t\t\treturn (AbstractDataNode) %s;\n", p.name));
            bw.write("\t\t}\n");
        }
        bw.write("\t\telse {\n");
        bw.write("\t\t\treturn super.getFirstOpen();\n");
        bw.write("\t\t}\n");
        bw.write("\t}\n\n");
    }


    private static Object capitalize(String n) {
        return Character.toUpperCase(n.charAt(0)) + n.substring(1);
    }

    private static void join(BufferedWriter bw, List<Param> params, String before, String between, 
            String after, boolean internals) throws IOException {
        List<String> l = new ArrayList<String>();
        for (Param p : params) {
            if (internals || !p.internal) {
                l.add(p.name);
            }
        }
        if (l.isEmpty()) {
            return;
        }
        bw.write(before);
        bw.write(l.get(0));
        if (l.size() != 1) {
            for (int i = 1; i < l.size(); i++) {
                bw.write(between);
                bw.write(l.get(i));
            }
        }
        bw.write(after);
    }

    private static String makeFileName(File f) {
        String abs = f.getAbsolutePath();
        int i = abs.lastIndexOf('.');
        return abs.substring(0, i) + "Params.java";
    }
}
