package com.anma.services

import com.anma.models.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kong.unirest.HttpResponse
import kong.unirest.Unirest
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CommentService {
    Gson gson = new GsonBuilder().setPrettyPrinting().create()
    final Logger LOGGER = LoggerFactory.getLogger(CommentService.class)


    def getPageComment(CONF_URL, TOKEN, commId) {
        def expand = "body.storage,version"
        def response = Unirest.get("${CONF_URL}/rest/api/content/${commId}?expand=${expand}")
                .header("Authorization", "Basic ${TOKEN}")
                .asString().body
        return gson.fromJson(response, Content.class)
    }

    def getPageComments(CONF_URL, TOKEN, sourceId) {
        //  GET /rest/api/content/{id}/child/comment
//        Content rootPage = getPage(CONF_URL, TOKEN, sourceId)
        def response = Unirest.get("${CONF_URL}/rest/api/content/" + sourceId + "/child/comment")
                .header("Authorization", "Basic ${TOKEN}")
                .asString().body
        return gson.fromJson(response, Contents.class)
    }

    // add existing comment to page
    def commentContains(CONF_URL, TOKEN, id, toFind) {
        def comment = getPageComment(CONF_URL, TOKEN, id)
        return comment.body.storage.value.contains(toFind)
    }

    // todo
    def addCommentToPage(CONF_URL, TOKEN, commId, tgtPageId, tgtURL, tgtTOKEN) {
        println("Adding comment ${commId} to page ${tgtPageId}")

        Content comment = getPageComment(CONF_URL, TOKEN, commId)

        Content newComment = new Content()
        newComment.title = comment.title
        newComment.type = 'comment'
        newComment.status = 'current'
//        Container container = new Container()   // Can be skipped
//        container.type = 'page'
//        container.id = pare
//        content.container = container
//        Ancestor ancestor = new Ancestor()
        Body commBody = new Body()
        Storage storage = new Storage()
        commBody.storage = storage
        storage.representation = 'storage'
        storage.value = comment.body.storage.value
        newComment.body = commBody
//        Container container = new Container()
        newComment.container = getPage(CONF_URL, TOKEN, tgtPageId)
//        Version version = new Version()
//        version.message = 'test'
//        version.number = 1
//        content.version = version
//        ancestor.id = parentId.toString()
//        Ancestor[] ancestors = [ancestor]
//        content.ancestors = ancestors

        def commJson = gson.toJson(newComment)

        return Unirest.post("${tgtURL}/rest/api/content")  // ext
                .header("Authorization", "Basic ${tgtTOKEN}")
                .body(commJson)
                .asString()
    }


    @Deprecated
    def deleteComment(CONF_URL, TOKEN, id) {
        println(">>>>>>> Performing DELETE COMMENT request")
        HttpResponse<String> response =
                Unirest.delete("${CONF_URL}/rest/api/content/${id}")
                        .header("Authorization", "Basic ${TOKEN}")
                        .asString()
        return response.body
    }

    @Deprecated
    def moveComment(CONF_URL, TOKEN, id, targetParentId) {
        Content comment = getComment(CONF_URL, TOKEN, id)
        Ancestor ancestor = new Ancestor()
        ancestor.id = targetParentId
        content.ancestors = [ancestor]
        content.version.number += 1

        println(gson.toJson(content))

        return Unirest.put("${CONF_URL}/rest/api/content/" + pageId)
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic ${TOKEN}")
                .body(gson.toJson(content))
                .asString()
                .body
    }

    def getCommentAttachments() {

    }

    def deletePageComments(CONF_URL, TOKEN, id) {
        println(">>>>>>> Performing DELETE COMMENTs request")
        // todo
    }

    @Deprecated
    def copyPageComments(CONF_URL, TOKEN, sourceId, targetId, tgtURL, tgtTOKEN) {
        Content[] comments = getPageComments(CONF_URL, TOKEN, sourceId).results

        if (comments.length > 0) {
            try {
                comments.each { comm ->
                    addCommentToPage(CONF_URL, TOKEN, comm.id, targetId, tgtURL, tgtTOKEN)  // add comments
                    LOGGER.info("Added comment ${comm.id} to ${targetId} page")
                }
            } catch (Exception e) {
                e.printStackTrace()
            }
        } else {
            LOGGER.info("${sourceId} page has 0 comments")
        }

    }


}