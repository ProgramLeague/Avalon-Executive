package ray.eldath.avalon.executive.test.core;

import org.eclipse.jetty.util.UrlEncoded;
import ray.eldath.avalon.executive.model.Submission;
import ray.eldath.avalon.executive.pool.LanguagePool;

class Protected {
    static final String workDir = "F:\\test";

    static final Submission py3 = new Submission(
            1,
            LanguagePool.getById("py3"),
            UrlEncoded.encodeString("print(\"Hello World\")"));
}
