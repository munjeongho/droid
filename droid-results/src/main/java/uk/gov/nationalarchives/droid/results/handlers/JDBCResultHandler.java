/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.gov.nationalarchives.droid.results.handlers;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationErrorType;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.core.interfaces.resource.ResourceUtils;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.profile.NodeMetaData;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * An implementation of the ResultHandler interface using JDBC to access the underlying profile database directly.
 *
 * @author Matt Palmer
 */
public class JDBCResultHandler implements ResultHandler {

    private Logger log = LoggerFactory.getLogger(getClass());

    private ResultHandlerDao resultHandlerDao;
    private ProgressMonitor progressMonitor;
    private Map<String, Format> formats;

    /**
     * Empty bean constructor.
     */
    public JDBCResultHandler() {
    }

    /**
     * Paramaterized constructor.
     * @param resultHandlerDao The resulthandlerdao to use.
     * @param progressMonitor The progress monitor to use.
     */
    public JDBCResultHandler(ResultHandlerDao resultHandlerDao, ProgressMonitor progressMonitor) {
        setResultHandlerDao(resultHandlerDao);
        setProgressMonitor(progressMonitor);
    }

    /**
     * Saves the incoming result to the database.
     *
     * @param results
     *            the results to be handled.
     * @return long
     *            node id.
     */
    @Override
    public ResourceId handle(IdentificationResultCollection results) {

        //log.debug(String.format("handling result for job [%s]", results.getUri()));

        ProfileResourceNode node = new ProfileResourceNode(results.getUri());

        RequestMetaData requestMetaData = results.getRequestMetaData();

        NodeMetaData metaData = new NodeMetaData();
        metaData.setLastModified(requestMetaData.getTime());
        metaData.setSize(results.getFileLength());
        metaData.setName(requestMetaData.getName());
        metaData.setExtension(ResourceUtils.getExtension(requestMetaData.getName()));
        metaData.setResourceType(results.isArchive() ? ResourceType.CONTAINER : ResourceType.FILE);
        metaData.setHash(requestMetaData.getHash());

        metaData.setNodeStatus(NodeStatus.DONE);

        node.setMetaData(metaData);
        node.setExtensionMismatch(results.getExtensionMismatch());
        node.setFinished(new Date());
        ResourceId parentId = results.getCorrelationId();
        if (results.getResults().isEmpty()) {
            node.addFormatIdentification(Format.NULL);
            node.setZeroIdentifications();
        } else {
            final List<IdentificationResult> theResults = results.getResults();
            final int numResults = theResults.size();
            // garbage reduction: use an indexed loop to avoid allocating an iterator.
            for (int i = 0; i < numResults; i++) {
                final IdentificationResult result = theResults.get(i);
                node.getMetaData().setIdentificationMethod(result.getMethod());
                final Format format = formats.get(result.getPuid());
                node.addFormatIdentification(format);
            }
        }
        resultHandlerDao.save(node, parentId);
        progressMonitor.stopJob(node);
        return new ResourceId(node.getId(), node.getPrefix());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleError(IdentificationException e) {
        final IdentificationRequest request = e.getRequest();
        final RequestIdentifier identifier = request.getIdentifier();
        final URI uri = identifier.getUri();
        final Long nodeId = identifier.getNodeId();

        final ProfileResourceNode node;
        if (nodeId != null) { // node already has an id - it has been saved already.
            node = resultHandlerDao.loadNode(nodeId);
            node.getMetaData().setNodeStatus(NodeStatus.ERROR);
        } else { // error occurred before the node was saved: make a new one for the resource:
            node = new ProfileResourceNode(uri);
            final NodeMetaData metaData = node.getMetaData();
            metaData.setResourceType(ResourceType.FILE);
            metaData.setNodeStatus(getNodeStatus(e.getErrorType()));
            node.setNoFormatsIdentified();
            RequestMetaData requestMetaData = request.getRequestMetaData();
            metaData.setName(requestMetaData.getName());
            metaData.setSize(requestMetaData.getSize());
            metaData.setExtension(request.getExtension());
            metaData.setLastModified(request.getRequestMetaData().getTime());
            metaData.setHash(requestMetaData.getHash());
            node.addFormatIdentification(Format.NULL); //TODO: check what happens with Format.NULL.
            node.setFinished(new Date());
        }
        resultHandlerDao.save(node, identifier.getParentResourceId());
        progressMonitor.stopJob(node);
    }



    private NodeStatus getNodeStatus(IdentificationErrorType error) {
        NodeStatus status;
        switch(error) {
            case ACCESS_DENIED:
                status = NodeStatus.ACCESS_DENIED;
                break;
            case FILE_NOT_FOUND:
                status = NodeStatus.NOT_FOUND;
                break;
            default:
                status = NodeStatus.ERROR;
        }
        return status;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceId handleDirectory(IdentificationResult result,
                                      ResourceId parentId, boolean restricted) {
        final URI uri = result.getIdentifier().getUri();
        //log.debug(String.format("handling directory [%s]", uri));
        ProfileResourceNode node = new ProfileResourceNode(uri);

        RequestMetaData requestMetaData = result.getMetaData();

        NodeMetaData metaData = new NodeMetaData();
        metaData.setName(requestMetaData.getName());
        metaData.setSize(null);
        metaData.setLastModified(requestMetaData.getTime());
        metaData.setIdentificationMethod(IdentificationMethod.NULL);
        metaData.setNodeStatus(restricted ? NodeStatus.ACCESS_DENIED : NodeStatus.DONE);
        metaData.setResourceType(ResourceType.FOLDER);
        node.setMetaData(metaData);
        node.setFinished(new Date());
        node.addFormatIdentification(Format.NULL);

        resultHandlerDao.save(node, parentId);
        progressMonitor.stopJob(node);
        return new ResourceId(node.getId(), node.getPrefix());
    }


    /**
     * @param resultHandlerDao
     *            the resultHandlerDao to set
     */
    public void setResultHandlerDao(ResultHandlerDao resultHandlerDao) {
        this.resultHandlerDao = resultHandlerDao;
    }

    /**
     * @param progressMonitor
     *            the progressMonitor to set
     */
    public void setProgressMonitor(ProgressMonitor progressMonitor) {
        this.progressMonitor = progressMonitor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteCascade(Long nodeId) {
        resultHandlerDao.deleteNode(nodeId);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void commit() {
        resultHandlerDao.commit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        formats = resultHandlerDao.getPUIDFormatMap();
    }
}
