package ray.eldath.avalon.executive.test.core;

import org.eclipse.jetty.util.UrlEncoded;
import ray.eldath.avalon.executive.model.Submission;
import ray.eldath.avalon.executive.pool.LanguagePool;

class Protected {
    static final Submission cpp11 = new Submission(
            0,
            LanguagePool.getById("cpp11"),
            UrlEncoded.encodeString("#include <cstdio>\n" +
                    "int main {\n" +
                    "   printf(\"Hello World\")\n" +
                    "}"));
    static final Submission py3 = new Submission(
            1,
            LanguagePool.getById("py3"),
            "print(\"Hello World\")");
}
