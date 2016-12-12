package org.springframework.blog.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.blog.bean.BlogBean;
import org.springframework.blog.bean.ResponseBean;
import org.springframework.blog.util.A;
import org.springframework.blog.util.TextUtils;
import org.springframework.blog.util.Tools;
import org.springframework.mvc.extensions.ajax.AjaxUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 博客操作controller
 * <p>
 * Created by liufu on 2016/12/9.
 */
@RestController
public class BlogController {
    //本地环境
    private String blogHome = "/Users/liufu/Documents/tmp";
    //线上环境
//    private String blogHome = "/usr/local/blog";

    private Gson gson = new Gson();

    @ModelAttribute
    public void ajaxAttribute(WebRequest request, Model model) {
        model.addAttribute("ajaxRequest", AjaxUtils.isAjaxRequest(request));
    }

    @ResponseBody
    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, value = "/blogAdd")
    public ResponseBean addBlog(HttpServletRequest request) throws IOException {
        request.setCharacterEncoding("UTF-8");
        ResponseBean responseBean = new ResponseBean();

        String json = Tools.getString(request);
        if (TextUtils.isEmpty(json)) {
            responseBean.setCode(A.code.FAILED);
            responseBean.setMsg("请求参数为空");
            return responseBean;
        }

        if (TextUtils.isEmpty(blogHome)) {
            responseBean.setCode(A.code.FAILED);
            responseBean.setMsg("请设置成员变量：BLOG_HOME");
            return responseBean;
        }
        File root = new File(blogHome);
        if (!root.exists()) {
            boolean mkdirs = root.mkdirs();
            if (!mkdirs) {
                responseBean.setCode(A.code.FAILED);
                responseBean.setMsg("创建Blog root错误");
                return responseBean;
            }
        }
        BlogBean blogBean = gson.fromJson(json, BlogBean.class);
        File file = new File(root, blogBean.getBlock());
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            if (!mkdirs) {
                responseBean.setCode(A.code.FAILED);
                responseBean.setMsg(String.format("创建Blog 模块%s错误", blogBean.getBlock()));
                return responseBean;
            }
        }
        File blog = new File(file, blogBean.getTitle() + A.SUB_FIX);
        if (blog.exists()) {
            responseBean.setCode(A.code.FAILED);
            responseBean.setMsg("该博客已存在");
            return responseBean;
        }
        boolean isSuccess = blog.createNewFile();
        if (!isSuccess) {
            responseBean.setCode(A.code.FAILED);
            responseBean.setMsg("博客提交失败");
            return responseBean;
        }
        try {
            FileOutputStream fos = new FileOutputStream(blog);
            fos.write(blogBean.getContent().getBytes("UTF-8"));
            fos.close();
        } catch (Exception e) {
            responseBean.setCode(A.code.FAILED);
            responseBean.setMsg(String.format("博客提交失败：%s", e.getMessage()));
            return responseBean;
        }
        responseBean.setMsg("博客发布成功");
        return responseBean;
    }

    @ResponseBody
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, value = "/blogList")
    public ResponseBean<List<BlogBean>> list(@RequestParam("blockPath") String blockPath)
            throws UnsupportedEncodingException {

        ResponseBean<List<BlogBean>> response = new ResponseBean<>();
        if (!"root".equals(blockPath)) {
            // tempUrl = blogHome.concat(String.format("%s%s", File.separator, path));
            response.setMsg("目前只有根目录可以查询");
            response.setCode(A.code.FAILED);
            return response;
        }
        List<BlogBean> dataList = new ArrayList<>();
        File file = new File(blogHome);
        File[] blocks = file.listFiles();
        if (blocks == null) {

            response.setCode(A.code.FAILED);
            response.setMsg("还没有模块信息");
            return response;
        }
        for (File f : blocks) {
            if (f.isDirectory() || f.getName().endsWith(A.SUB_FIX)) {

                BlogBean block = new BlogBean();
                File[] children = f.listFiles();
                String blockName = f.getName();
                if (children != null && children.length != 0) {

                    List<BlogBean> list = new ArrayList<>();
                    for (File child : children) {
                        String name = child.getName();
                        if (!TextUtils.isEmpty(name) && name.endsWith(A.SUB_FIX)) {

                            BlogBean c = new BlogBean();
                            c.setBlock(blockName);
                            c.setTitle(name.split("\\.")[0]);
                            list.add(c);
                        }
                    }
                    block.setChildren(list);
                }
                block.setTitle(blockName);
                dataList.add(block);
            }
        }
        response.setMsg("请求成功");
        response.setContent(dataList);
        return response;
    }


    @ResponseBody
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, value = "/blogContent")
    public ResponseBean<String> getContent(@RequestParam("block") String block, @RequestParam("title") String title)
            throws FileNotFoundException, UnsupportedEncodingException {

        ResponseBean<String> response = new ResponseBean<>();
        String filePath = blogHome.concat(File.separator).concat(block).concat(File.separator)
                .concat(title).concat(A.SUB_FIX);
        File file = new File(filePath);

        if (!file.exists()) {
            response.setCode(A.code.FAILED);
            response.setMsg("博客不存在");
        }

        FileInputStream fis = new FileInputStream(file);

        byte[] buffer = new byte[512];
        int len;
        StringBuffer sb = new StringBuffer();
        try {
            while ((len = fis.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, len));
            }
            response.setContent(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        response.setMsg("查询成功");
        return response;
    }
}
