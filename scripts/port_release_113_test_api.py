from pathlib import Path
R=Path(__file__).resolve().parents[1]/'versions/1.1.3'
for v in ['1.21.11','1.21.8','1.21.4','1.21.1','1.20.4','1.20.1']:
 p=R/('mc'+v)/'src/test/java/cn/quit5700/light/logic/AreaLightInitializationTest.java'
 s=p.read_text(encoding='utf-8')
 s=s.replace('import java.lang.classfile.ClassFile;\nimport java.lang.classfile.ClassModel;\nimport java.lang.classfile.MethodModel;\nimport java.lang.classfile.instruction.InvokeInstruction;', 'import org.objectweb.asm.ClassReader;\nimport org.objectweb.asm.tree.ClassNode;\nimport org.objectweb.asm.tree.MethodNode;\nimport org.objectweb.asm.tree.MethodInsnNode;')
 s=s.replace('ClassModel','ClassNode').replace('MethodModel','MethodNode').replace('InvokeInstruction','MethodInsnNode')
 s=s.replace('return ClassFile.of().parse(input.readAllBytes());','ClassNode node=new ClassNode();\n            new ClassReader(input).accept(node,0);\n            return node;')
 s=s.replace('method.code().orElseThrow().elementList().stream()', 'java.util.Arrays.stream(method.instructions.toArray())')
 s=s.replace('.methods()', '.methods').replace('.methodName().equalsString(', '.name.equals(').replace('.methodType().equalsString(', '.desc.equals(')
 s=s.replace('.owner().asInternalName()', '.owner').replace('.name().equalsString(', '.name.equals(').replace('.type().stringValue()', '.desc').replace('.name().stringValue()', '.name').replace('.getFirst()', '.get(0)')
 p.write_text(s,encoding='utf-8')
 p=R/('mc'+v)/'build.gradle';s=p.read_text(encoding='utf-8')
 if "testImplementation 'org.ow2.asm:asm-tree:" not in s:s=s.replace("testImplementation 'org.junit.jupiter:junit-jupiter'", "testImplementation 'org.junit.jupiter:junit-jupiter'\n    testImplementation 'org.ow2.asm:asm-tree:9.7.1'")
 p.write_text(s,encoding='utf-8')
