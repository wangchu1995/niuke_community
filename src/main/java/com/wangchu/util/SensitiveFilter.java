package com.wangchu.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

@Component
public class SensitiveFilter {
    //1.定义前缀树
    private class TrieNode{
        //关键词结束标识
        private boolean isKeywordEnd = false;
        //子节点
        private HashMap<Character,TrieNode> subNodes = new HashMap<Character, TrieNode>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c,node);
        }

        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    private static final String REPLACEMENT = "*";
    private TrieNode root = new TrieNode();

    @PostConstruct
    public void init(){
        //Bean实例化，构造器构造以后
        //初始化，从文件中读取敏感词，添加到前缀树结构
        //1.获取文件输入流
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is));)
        {
            //将读取到的单词陆续加入前缀树
            String keyword;
            while((keyword=reader.readLine())!=null){
                this.addKeyword(keyword);
            }

        } catch (Exception e) {
            logger.debug("加载敏感词失败 "+e);
        }

    }

    private void addKeyword(String keyword){
        TrieNode tempNode = root;
        for (int i = 0; i < keyword.length(); i++) {
            Character c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if(subNode==null){
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            tempNode = subNode;
            //设置结束标志
            if(i==keyword.length()-1) tempNode.setKeywordEnd(true);
        }
    }

    public String filter(String text){
        //对字符串进行前缀树敏感词过滤
        if(StringUtils.isBlank(text)){
            return null;
        }
        //2.声明变量
        TrieNode tempNode = root;
        int begin = 0;
        int position = 0;
        StringBuilder sb = new StringBuilder();
        //3.处理字符串
        while(position<text.length()){
            Character c = text.charAt(position);
            //跳过字符
            if(isSymbol(c)){
                if(tempNode==root){
                    //begin必须开始于非符号位置？root必须变化
                    sb.append(c);
                    begin++;
                }
                    //跳过，不加入sb
                    position++;
                    continue;
                }
            //检查下级节点
            tempNode = tempNode.getSubNode(c);
            if(tempNode==null){
                //前缀树不匹配
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode=root;
            }else if(tempNode.isKeywordEnd){
                //匹配到一个敏感词
                sb.append(REPLACEMENT);
                begin=++position;
                tempNode=root;
            }else{
                //没有检测完敏感词，继续检查下一个字符
                position++;
            }
            }
        //将最后一批字符计入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }

    private boolean isSymbol(Character c){
        return !CharUtils.isAsciiAlphanumeric(c)&&(c<0x2E80||c>0x9FFF);
    }

}
