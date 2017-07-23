package ray.eldath.avalon.executive.servlet;

import com.spotify.docker.client.exceptions.DockerException;
import org.eclipse.jetty.util.UrlEncoded;
import org.json.JSONObject;
import org.json.JSONTokener;
import ray.eldath.avalon.executive.core.Runner;
import ray.eldath.avalon.executive.exception.RunErrorException;
import ray.eldath.avalon.executive.model.ExecPair;
import ray.eldath.avalon.executive.model.Submission;
import ray.eldath.avalon.executive.pool.ExecutableFilePool;
import ray.eldath.avalon.executive.pool.SubmissionPool;
import ray.eldath.avalon.executive.tool.ResponseRequestUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public class Run extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        JSONObject object = (JSONObject) new JSONTokener(req.getReader()).nextValue();
        int id = object.getInt("id");
        Submission submission = SubmissionPool.getById(id);
        if (submission == null) {
            ResponseRequestUtils.response(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "找不到ID为" + id + "的提交！请检查您是否调用过API - compile？"
            );
            return;
        }
        SubmissionPool.rm(id);

        File executableFile = ExecutableFilePool.get(id);
        if (executableFile == null) {
            ResponseRequestUtils.response(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "找不到对应的可执行文件！请确认已成功执行过API - compile！"
            );
            return;
        }
        ExecutableFilePool.rm(id);

        JSONObject response = new JSONObject();
        ExecPair result;

        response.put("id", id);
        try {
            result = Runner.run(submission.getLanguage(), executableFile);
        } catch (DockerException | InterruptedException e) {
            ResponseRequestUtils.responseDockerException(resp, e);
            return;
        } catch (RunErrorException e) {
            response.put("error", true);
            response.put("return", UrlEncoded.encodeString(e.toString()));
            resp.getWriter().write(response.toString());
            resp.getWriter().close();
            return;
        }
        response.put("error", false);
        response.put("return", UrlEncoded.encodeString(result.getLog()));
        resp.getWriter().write(response.toString());
        resp.getWriter().close();
    }
}
