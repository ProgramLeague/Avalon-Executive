package ray.eldath.avalon.executive.servlet;

import org.json.JSONObject;
import org.json.JSONTokener;
import ray.eldath.avalon.executive.model.Language;
import ray.eldath.avalon.executive.model.Submission;
import ray.eldath.avalon.executive.pool.LanguagePool;
import ray.eldath.avalon.executive.tool.ResponseErrorMessage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Compile extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        JSONObject object = (JSONObject) new JSONTokener(req.getReader()).nextValue();
        int id = object.getInt("id");
        String encodedCode = object.getString("code");
        String languageId = object.getString("lang");
        Language language = LanguagePool.getById(languageId);
        if (language == null) {
            ResponseErrorMessage.response(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "找不到id为" + languageId + "的语言！"
            );
            return;
        }
        Submission submission = new Submission(id, language, encodedCode);
    }
}
