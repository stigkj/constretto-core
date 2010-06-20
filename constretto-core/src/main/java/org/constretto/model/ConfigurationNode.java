/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constretto.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
public class ConfigurationNode {
    public static final String DEFAULT_TAG = "[default-tag]";
    public static final String ALL_TAG = "[all-tag]";
    private static final String ROOT_ELEMENT_NAME = "root-element";
    private final String name;
    private final String tag;
    private final List<ConfigurationNode> children = new ArrayList<ConfigurationNode>();
    private ConfigurationNode parent;
    private String value;


    public static ConfigurationNode createRootElement() {
        return new ConfigurationNode();
    }

    public static void createRootElementOf(ConfigurationNode currentConfigurationNode) {
        ConfigurationNode newRoot = new ConfigurationNode();
        for (ConfigurationNode childNode : currentConfigurationNode.children) {
            newRoot.addChild(childNode);
        }
    }

    public ConfigurationNode root() {
        ConfigurationNode currentNode = this;
        while (currentNode.parent != null) {
            currentNode = currentNode.parent;
        }
        return currentNode;
    }

    public String getValue() {
        return value;
    }

    public String getTag() {
        return tag;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public List<ConfigurationNode> children() {
        return children;
    }

    public String getExpression() {
        String expression;
        if (parent != null && !parent.name.equals(ROOT_ELEMENT_NAME)) {
            expression = parent.getExpression() + "." + name;
        } else {
            expression = name;
        }
        return expression;
    }

    public void update(String expression, String value, String tag) {
        ConfigurationNode parent = this;
        String name = expression;
        if (expression.contains(".")) {
            parent = findOrCreateParent(expression.substring(0, expression.lastIndexOf(".")));
            name = expression.substring(expression.lastIndexOf(".") + 1, expression.length());
        }
        ConfigurationNode leaf = new ConfigurationNode(name, tag, value);
        parent.addChild(leaf);
    }


    public List<ConfigurationNode> findAllBy(String expression) {
        ConfigurationNode currentNode = this;
        for (String subExpression : expression.split("\\.")) {
            if (currentNode.containsChild(subExpression)) {
                currentNode = currentNode.getFirstChild(subExpression);
            } else {
                return Collections.emptyList();
            }
        }
        return currentNode.parent.getAllMatchingChildren(currentNode.name);
    }

    private ConfigurationNode() {
        this.name = ROOT_ELEMENT_NAME;
        this.tag = DEFAULT_TAG;
    }

    private ConfigurationNode(String name, String tag, String value) {
        this.name = name;
        this.tag = tag;
        this.value = value;
    }

    private ConfigurationNode(String name) {
        this.name = name;
        this.tag = DEFAULT_TAG;
    }

    private boolean containsChild(String name) {
        for (ConfigurationNode currentNode : children) {
            if (currentNode.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private ConfigurationNode getFirstChild(String name) {
        for (ConfigurationNode currentNode : children) {
            if (currentNode.name.equals(name)) {
                return currentNode;
            }
        }
        return null;
    }

    private List<ConfigurationNode> getAllMatchingChildren(String name) {
        List<ConfigurationNode> matches = new ArrayList<ConfigurationNode>();
        for (ConfigurationNode currentNode : children) {
            if (currentNode.name.equals(name)) {
                matches.add(currentNode);
            }
        }
        return matches;
    }


    private ConfigurationNode findOrCreateParent(String expression) {
        ConfigurationNode currentNode = this;
        for (String subExpression : expression.split("\\.")) {
            if (!currentNode.containsChild(subExpression)) {
                ConfigurationNode newNode = new ConfigurationNode(subExpression);
                currentNode.addChild(newNode);
                currentNode = newNode;
            } else {
                currentNode = currentNode.getFirstChild(subExpression);
            }
        }
        return currentNode;
    }

    private void addChild(ConfigurationNode configurationNode) {
        children.add(configurationNode);
        configurationNode.parent = this;
    }

    @Override
    public String toString() {
        return "ConfigurationNode{" +
                "expression='" + name + '\'' +
                ", value='" + value + '\'' +
                ", tag='" + tag + '\'' +
                '}';
    }
}
