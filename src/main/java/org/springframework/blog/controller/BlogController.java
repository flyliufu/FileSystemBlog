package org.springframework.blog.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;
import org.springframework.blog.bean.BlogBean;
import org.springframework.blog.bean.ResponseBean;
import org.springframework.blog.util.A;
import org.springframework.blog.util.TextUtils;
import org.springframework.blog.util.Tools;
import org.springframework.mvc.extensions.ajax.AjaxUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;

/**
 * 博客操作controller
 * <p>
 * Created by liufu on 2016/12/9.
 */
@RestController
public class BlogController {
    private Logger logger = Logger.getLogger(this.getClass());
    //本地环境
    private String blogHome = "/Users/liufu/Documents/tmp/";
    //线上环境
//    private String blogHome = "/usr/local/blog";

    private Gson gson = new Gson();

    @ModelAttribute
    public void ajaxAttribute(WebRequest request, Model model) {
        model.addAttribute("ajaxRequest", AjaxUtils.isAjaxRequest(request));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/blogAdd")
    public ResponseBean addBlog(HttpServletRequest request) throws IOException {
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
        String fileName = new String(blogBean.getTitle().getBytes(), "UTF-8") + A.SUB_FIX;
        logger.info(fileName);

        File blog = new File(file, fileName);
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

    @RequestMapping(method = RequestMethod.POST, value = "/blogList")
    public ResponseBean<List<BlogBean>> list(HttpServletRequest request) throws IOException {

        ResponseBean<List<BlogBean>> response = new ResponseBean<>();
        String json = Tools.getString(request);
        if (TextUtils.isEmpty(json)) {
            response.setCode(A.code.FAILED);
            response.setMsg("请求参数为空");
            return response;
        }

        Map<String, String> map = gson.fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType());

        if (!map.containsKey("blockPath") && !"root".equals(map.get("blockPath"))) {
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

    @RequestMapping(method = RequestMethod.POST, value = "/blogContent")
    public ResponseBean<String> getContent(HttpServletRequest request) throws IOException {
        ResponseBean<String> response = new ResponseBean<>();

        String json = Tools.getString(request);
        if (TextUtils.isEmpty(json)) {
            response.setCode(A.code.FAILED);
            response.setMsg("请求参数为空");
            return response;
        }

        BlogBean blogBean = gson.fromJson(json, BlogBean.class);
        String filePath = blogHome.concat(File.separator).concat(blogBean.getBlock())
                .concat(File.separator).concat(blogBean.getTitle()).concat(A.SUB_FIX);
        File file = new File(filePath);

        if (!file.exists()) {
            response.setCode(A.code.FAILED);
            response.setMsg("博客不存在");
            return response;
        }

        response.setContent(Tools.readString4File(file));
        response.setMsg("查询成功");
        return response;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/blogTag")
    public ResponseBean<String[]> getTag(HttpServletRequest request) throws IOException {
        ResponseBean<String[]> response = new ResponseBean<>();

        String filePath = blogHome.concat(File.separator).concat(A.CONF_FILE_NAME).concat(File.separator).concat(A.CONF_TAG_NAME);
        File file = new File(filePath);

        if (!file.exists()) {
            response.setCode(A.code.FAILED);
            response.setMsg("标签配置不存在");
            return response;
        }
        String json = Tools.readString4File(file);
        String[] list = gson.fromJson(json, new TypeToken<String[]>() {
        }.getType());
        response.setContent(list);
        response.setMsg("查询成功");
        return response;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/savPic")
    public ResponseBean<String> savePic(MultipartHttpServletRequest request) {
        ResponseBean<String> response = new ResponseBean<>();

        Set set = request.getFileMap().entrySet();
        for (Object aSet : set) {
            Map.Entry me = (Map.Entry) aSet;
            String fileName = (String) me.getKey();
            MultipartFile multipartFile = (MultipartFile) me.getValue();
            logger.info("Original fileName - " + multipartFile.getOriginalFilename());
            logger.info("fileName - " + fileName);
            writeToDisk(fileName, multipartFile);
        }
        if (set.size() == 0) {
            response.setMsg("没有相关文件上传");
            response.setCode(A.code.FAILED);
        } else {
            response.setMsg("上传成功");
        }
        return response;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/localUpload")
    public ResponseBean<String> localUpload(@RequestParam("name") String name,
                                            @RequestParam("file") MultipartFile file,
                                            MultipartHttpServletRequest request) {
        ResponseBean<String> response = new ResponseBean<>();

        writeToDisk(file.getName() + "1", file);
        response.setCode(A.code.SUCCESS);
        response.setMsg("上传成功");
        response.setContent(file.getName());
        return response;
    }

    public void writeToDisk(String filename, MultipartFile multipartFile) {
        try {
            String fullFileName = blogHome + filename;
            FileOutputStream fos = new FileOutputStream(fullFileName);
            fos.write(multipartFile.getBytes());
            fos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
