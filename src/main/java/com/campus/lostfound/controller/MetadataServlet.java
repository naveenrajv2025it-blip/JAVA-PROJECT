package com.campus.lostfound.controller;

import com.campus.lostfound.dao.DAOFactory;
import com.campus.lostfound.model.Category;
import com.campus.lostfound.model.Location;
import com.campus.lostfound.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/metadata/*")
public class MetadataServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if ("/categories".equalsIgnoreCase(pathInfo)) {
            List<Category> categories = DAOFactory.getCategoryDAO().getAllCategories();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, categories);
            return;
        }

        if ("/locations".equalsIgnoreCase(pathInfo)) {
            List<Location> locations = DAOFactory.getLocationDAO().getAllLocations();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, locations);
            return;
        }

        // Return combined
        Map<String, Object> data = new HashMap<>();
        data.put("categories", DAOFactory.getCategoryDAO().getAllCategories());
        data.put("locations", DAOFactory.getLocationDAO().getAllLocations());
        JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }
}
