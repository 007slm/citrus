˵��:
======

����Ŀ�Ǵ�ObjectWeb ASM v3.1(http://asm.objectweb.org/license.html)����ֲ�������������޸ģ�

* �޸�package
  org.objectweb.asm -> com.alibaba.citrus.asm
  org/objectweb/asm -> com/alibaba/citrus/asm

* �޸ĵ�Ԫ���ԣ�
  1. ֻ������conform���ԡ�
  2. ����AsmTestParams�����ࡣ
  3. �޸�AbstractTest�࣬ʹ֮��
     # ʹ��AsmTestParams������System properties��
     # �Ľ�suite�����ƣ����ϡ�-part��
     # ��stream�ĳ�URL�������ڴ����
     # ���exclude�������ų�������ļ�
     # �ر�zip��
     # ����һ��suite��test���������
  4. �޸���ASMifierUnitTest��CheckClassAdapterUnitTest��TraceClassAdapterUnitTest�д���main�����Ĳ�����
  5. �޸���ASMifierTest��GASMifierTest�еĲ�����clazz=java.lang
  6. �޸���ClassWriterComputeFramesTest��LocalVariablesSorterTest2��SimpleVerifierTest�еĲ�����parts=2
  7. �޸����ж�AbstractTest.is�����ã��ĳ�openStream()���á�
  8. �޸���SerialVersionUIDAdderUnitTest�е�UID���Ա�ͨ�����ԡ�
  9. ClassNodeTest�У�����InsnList.check = false���Ա�ͨ�����ԡ�
  
* ��StringBuffer�ĳ�StringBuilder

* Cleanup

* ����Ҫ�㣺�ڴ�����ǳ�����÷���1G-2G�Ŀռ䣺-Xms1536M -Xmx1536M -XX:MaxPermSize=512M

