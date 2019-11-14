package com.seeyoo.mps.tool;

import com.seeyoo.mps.vo.Tree;
import com.seeyoo.mps.vo.router.RouterMeta;
import com.seeyoo.mps.vo.router.VueRouter;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class TreeUtil {

    protected TreeUtil() {

    }

    private final static String TOP_NODE_ID = "0";

    /**
     * 用于构建菜单或部门树
     *
     * @param nodes nodes
     * @param <T>   <T>
     * @return <T> Tree<T>
     */
    public static <T> Tree<T> build(List<Tree<T>> nodes) {
        if (nodes == null) {
            return null;
        }
        List<Tree<T>> topNodes = new ArrayList<>();
        nodes.forEach(node -> {
            String pid = node.getParentId();
            if (pid == null || TOP_NODE_ID.equals(pid)) {
                topNodes.add(node);
                return;
            }
            for (Tree<T> n : nodes) {
                String id = n.getId();
                if (id != null && id.equals(pid)) {
                    if (n.getChildren() == null)
                        n.initChildren();
                    n.getChildren().add(node);
                    node.setHasParent(true);
                    n.setHasChildren(true);
                    n.setHasParent(true);
                    return;
                }
            }
            if (topNodes.isEmpty())
                topNodes.add(node);
        });


        Tree<T> root = new Tree<>();
        root.setId("0");
        root.setParentId("");
        root.setHasParent(false);
        root.setHasChildren(true);
        root.setChildren(topNodes);
        root.setText("root");
        return root;
    }


    /**
     * 构造前端路由
     *
     * @param routes routes
     * @param <T>    T
     * @return ArrayList<VueRouter<T>>
     */
    public static <T> ArrayList<VueRouter<T>> buildVueRouter(List<VueRouter<T>> routes) {
        if (routes == null) {
            return null;
        }
        List<VueRouter<T>> topRoutes = new ArrayList<>();
        VueRouter<T> router = new VueRouter<>();
        router.setName("welcome");
        router.setPath("/welcome");
        router.setComponent("welcome/welcome");
//        router.setIcon("home");
        router.setHidden(true);
        router.setChildren(null);
//        router.setMeta(new RouterMeta(false, true,"首页",null,null));
        topRoutes.add(router);

        routes.forEach(route -> {
            String parentId = route.getParentId();
            if (StringUtils.isEmpty(route.getParentId()) || TOP_NODE_ID.equals(parentId)) {
                topRoutes.add(route);
                return;
            }
            for (VueRouter<T> parent : routes) {
                String id = parent.getId();
                if (id != null && id.equals(parentId)) {
                    if (parent.getChildren() == null)
                        parent.initChildren();
                    parent.getChildren().add(route);
                    parent.setHasChildren(true);
                    route.setHasParent(true);
                    parent.setHasParent(true);
                    return;
                }
            }
        });
    /*    router = new VueRouter<>();
        router.setPath("/profile");
        router.setName("个人中心");
        router.setComponent("personal/Profile");
        router.setIcon("none");
        router.setMeta(new RouterMeta(true, false));
        topRoutes.add(router);*/

        ArrayList<VueRouter<T>> list = new ArrayList<>();
        VueRouter<T> root = new VueRouter<>();
        root.setName("index");
        root.setComponent("BasicLayout");
//        root.setIcon("none");
        root.setPath("/");
        root.setRedirect("/welcome");
        root.setMeta(new RouterMeta(null,"首页",null,null,null));
        root.setChildren(topRoutes);
        list.add(root);

        root = new VueRouter<>();
        root.setName("404");
        root.setComponent("404");
        root.setPath("*");
        root.setRedirect("/404");
        list.add(root);

        return list;
    }
}