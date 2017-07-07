package ray.eldath.avalon.executive.servlet;

import org.json.JSONArray;
import org.json.JSONObject;
import ray.eldath.avalon.executive.model.Language;
import ray.eldath.avalon.executive.pool.LanguagePool;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class GetAllLang extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        JSONObject object = new JSONObject();
        JSONArray array = new JSONArray();
        List<Language> allLanguage = LanguagePool.getAllLanguage();
        for (Language thisLanguage : allLanguage) {
            JSONObject child = new JSONObject();
            child.put("id", thisLanguage.getId());
            child.put("name", thisLanguage.getName());
            array.put(child);
        }
        object.put("lang", array);
        resp.setContentType("application/json");
        object.write(resp.getWriter());
    }
}
