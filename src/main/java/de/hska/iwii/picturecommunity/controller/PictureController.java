package de.hska.iwii.picturecommunity.controller;

import de.hska.iwii.picturecommunity.backend.dao.PictureDAO;
import de.hska.iwii.picturecommunity.backend.entities.Picture;
import de.hska.iwii.picturecommunity.backend.entities.User;
import de.hska.iwii.picturecommunity.backend.utils.ImageUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
@Scope("session")
public class PictureController {

    static final int THUMBNAIL_SIZE = 100;

    /**
     * DAO for pictures
     */
    @Autowired
    private PictureDAO pictureDAO;

    /**
     * User Access Login Controller
     */
    @Autowired
    private LoginController userMgr;

    private Picture selectedPicture;
    private UploadedFile uploadFile;
    private boolean isUploadPublic = true;
    private String uploadTitle;
    private String uploadDescription;

    public String getUploadTitle() {
        return uploadTitle;
    }

    public void setUploadTitle(String uploadTitle) {
        this.uploadTitle = uploadTitle;
    }

    public String getUploadDescription() {
        return uploadDescription;
    }

    public void setUploadDescription(String uploadDescription) {
        this.uploadDescription = uploadDescription;
    }

    public boolean isUploadPublic() {
        return isUploadPublic;
    }

    public void setUploadPublic(boolean uploadPublic) {
        this.isUploadPublic = uploadPublic;
    }

    public UploadedFile getUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(UploadedFile uploadFile) {
        this.uploadFile = uploadFile;
    }

    public Picture getSelectedPicture() {
        Logger l = LoggerFactory.getLogger(this.getClass());
        if (selectedPicture != null) {
            l.info("get selected picture: " + selectedPicture.getName());
        } else {
            l.info("get selected picture: None");
        }
        return selectedPicture;
    }

    public void setSelectedPicture(Picture selectedPicture) {
        LoggerFactory.getLogger(this.getClass()).info("set selected picture: " + selectedPicture.getName());
        this.selectedPicture = selectedPicture;
    }

    /**
     * Updates the selected picture in the database.
     * <p>
     * This method is used as callback function if the properties of the selected image are changed in the detail view.
     */
    public void updateSelectedPicture() {
        LoggerFactory.getLogger(this.getClass()).info("Update picture " + selectedPicture.getName());
    }

    /**
     * Get all pictures of the current user.
     *
     * @param user the user whose pictures are to be returned. If this parameter is <code>null</code>, all public
     *             pictures are returned.
     * @return the list of pictures of the current user, or null if user is not logged in.
     */
    public List<Picture> getPictures(User user) {
        boolean excludePrivatePictures = true;

        if (user != null && userMgr.getLoggedIn()) {
            User currentUser = userMgr.getInstance();

            if (currentUser.equals(user) || user.getFriendsOf().contains(currentUser)) {
                excludePrivatePictures = false;
            }
        }
        return pictureDAO.getPictures(user, 0, Integer.MAX_VALUE, excludePrivatePictures);
    }

    /**
     * Returns the picture specified by the request parameter <code>id</code> as byte stream.
     *
     * @return the image data.
     */
    public StreamedContent getPicture() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        if (ctx.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {

            return new DefaultStreamedContent();
        } else {
            String id = ctx.getExternalContext().getRequestParameterMap().get("id");
            try
            {
                Picture pic = pictureDAO.getPicture(Integer.parseInt(id));

                return new DefaultStreamedContent(new ByteArrayInputStream(pic.getData()));
            }
            catch (NumberFormatException e)
            {
            	return null;
            }
        }
    }

    /**
     * Picture “upload handler”.
     */
    public void doUpload() {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        User user = userMgr.getInstance();
        Picture pic = new Picture();
        int size = (int) uploadFile.getSize();
        byte[] data;

        logger.info("uploading picture: " + uploadFile.getFileName());
        pic.setName(uploadTitle.isEmpty() ? uploadFile.getFileName() : uploadTitle);
        pic.setMimeType(uploadFile.getContentType());
        pic.setDescription(uploadDescription);
        pic.setPublicVisible(isUploadPublic);
        pic.setCreator(user);
        data = uploadFile.getContents();
        if (data == null || data.length != size) {
            try {
                data = new byte[size];
                InputStream is = uploadFile.getInputstream();

                if (is.read(data, 0, size) != size) {
                    throw new IOException("Not enough data available"); // ugh
                }
            } catch (IOException e) {
                logger.warn("caught an exception while uploading: " + e.getMessage());
                data = null;
            }
        }
        if (data != null && data.length == size) {
            long pc = pictureDAO.getPictureCount(user);

            pic.setData(data);
            pictureDAO.createPicture(user, pic);
            if (pictureDAO.getPictureCount(user) == pc) {
                logger.warn("failed to upload image");
            }
        } else {
            logger.warn("Picture data was invalid, nothing uploaded.");
        }
        uploadTitle = "";
        uploadDescription = "";
        uploadFile = null;
    }

    /**
     * Returns the thumbnail of the picture specified by the request parameter <code>id</code> as byte stream.
     *
     * @return the thumbnail image data
     */
    public StreamedContent getThumbnail() {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        FacesContext ctx = FacesContext.getCurrentInstance();

        logger.info("getThumbnail");
        if (ctx.getCurrentPhaseId() != PhaseId.RENDER_RESPONSE) {
            byte[] d;
            logger.info("getting id…");
            String idstr = ctx.getExternalContext().getRequestParameterMap().get("id");
            logger.info("id is: " + idstr);
            logger.info("getting picture…");
            Picture pic = null;
            try {
                pic = pictureDAO.getPicture(Integer.parseInt(idstr));
            } catch (NumberFormatException e) {
                logger.warn("received no valid picture id");
            }
            logger.info("picture is: " + pic);

            if (pic != null) {
                try {
                    d = ImageUtils.scale(pic.getData(), THUMBNAIL_SIZE, null);
                } catch (IOException e) {
                    logger.warn("An exception was catched during resizing");
                    d = pic.getData();
                }

                return new DefaultStreamedContent(new ByteArrayInputStream(d));
            }
            logger.warn("invalid picture id " + idstr);
        }
        logger.info("no picture");
        return null;
    }

}
