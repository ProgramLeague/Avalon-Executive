package ray.eldath.avalon.executive.servlet;

import com.spotify.docker.client.exceptions.DockerException;
import org.eclipse.jetty.util.UrlEncoded;
import org.json.JSONObject;
import org.json.JSONTokener;
import ray.eldath.avalon.executive.core.Compiler;
import ray.eldath.avalon.executive.core.PreProcessor;
import ray.eldath.avalon.executive.exception.CompileErrorException;
import ray.eldath.avalon.executive.model.Language;
import ray.eldath.avalon.executive.model.Submission;
import ray.eldath.avalon.executive.pool.ExecutableFilePool;
import ray.eldath.avalon.executive.pool.LanguagePool;
import ray.eldath.avalon.executive.pool.SubmissionPool;
import ray.eldath.avalon.executive.tool.ResponseRequestUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public class Compile extends HttpServlet {
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json");
		JSONObject object = (JSONObject) new JSONTokener(req.getReader()).nextValue();
		int id = object.getInt("id");
		String encodedCode = object.getString("code");
		String languageId = object.getString("lang");
		Language language = LanguagePool.getById(languageId);
		if (language == null) {
			ResponseRequestUtils.response(
					resp,
					HttpServletResponse.SC_BAD_REQUEST,
					"找不到id为" + languageId + "的语言！"
			);
			return;
		}
		Submission submission = new Submission(id, language, encodedCode);
		SubmissionPool.put(submission);
		JSONObject response = new JSONObject();
		response.put("id", id);
		File executableFile;
		try {
			PreProcessor.instance().createFile(submission);
			executableFile = Compiler.compile(submission);
		} catch (DockerException | InterruptedException e) {
			ResponseRequestUtils.responseDockerException(resp, e);
			return;
		} catch (CompileErrorException e) {
			response.put("out", UrlEncoded.encodeString(e.toString()));
			response.put("error", true);
			resp.getWriter().write(response.toString());
			resp.getWriter().close();
			return;
		}
		ExecutableFilePool.put(id, executableFile);
		response.put("out", "");
		response.put("error", false);
		resp.getWriter().write(response.toString());
		resp.getWriter().close();
	}
}
