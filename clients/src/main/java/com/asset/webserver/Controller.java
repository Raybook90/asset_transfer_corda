package com.asset.webserver;
import com.asset.Initiator;
import com.asset.Transfer;
import com.asset.webserver.Objects.Asset;
import com.asset.webserver.Objects.TransferAsset;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Define your API endpoints here.
 */


@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
public class Controller {
    private final CordaRPCOps proxy;
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);

    public Controller(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
    }

    @GetMapping(value = "/templateendpoint", produces = "text/plain")
    private String templateendpoint() {
        return "Define an endpoint here.";
    }

    @GetMapping(value = "/test", produces = "text/plain")
    private String test() {
        return "Test an endpoint here."+ proxy.currentNodeTime();
    }

    @PostMapping(path = "/register", produces = "text/plain")
    public ResponseEntity register(@RequestBody Asset asset) {
        //code
        //System.out.println(state.getName());
        //logger.info(state.getName());

        final Party owner = proxy.wellKnownPartyFromX500Name(new CordaX500Name(asset.getOwner().getName(),asset.getOwner().getLocality(),asset.getOwner().getCountry()));
        final Party validator = proxy.wellKnownPartyFromX500Name(new CordaX500Name(asset.getValidator().getName(),asset.getValidator().getLocality(),asset.getValidator().getCountry()));
        try {
            final SignedTransaction signedTx = proxy
                    .startTrackedFlowDynamic(Initiator.class, owner, asset.getName(),  validator)
                    .getReturnValue()
                    .get();

            final String msg = String.format("Transaction id %s committed to ledger.\n", signedTx.getId());
            //return Response.status(CREATED).entity(msg).build();
            return new ResponseEntity(msg, HttpStatus.OK);

        } catch (Throwable ex) {
            final String msg = ex.getMessage();
            logger.error(ex.getMessage(), ex);
            return new ResponseEntity(ex, HttpStatus.OK);
            //return Response.status(BAD_REQUEST).entity(msg).build();
        }
    }

    @PostMapping(path = "/transfer", produces = "text/plain")
    public ResponseEntity transfer(@RequestBody TransferAsset asset) {
        //code
        //System.out.println(state.getName());
        //logger.info(state.getName());

        final Party newOwner = proxy.wellKnownPartyFromX500Name(new CordaX500Name(asset.getNewOwner().getName(),asset.getNewOwner().getLocality(),asset.getNewOwner().getCountry()));
        try {
            final SignedTransaction signedTx = proxy
                    .startTrackedFlowDynamic(Transfer.class, asset.getName(),  newOwner)
                    .getReturnValue()
                    .get();

            final String msg = String.format("Transaction id %s committed to ledger.\n", signedTx.getId());
            //return Response.status(CREATED).entity(msg).build();
            return new ResponseEntity(msg, HttpStatus.OK);

        } catch (Throwable ex) {
            final String msg = ex.getMessage();
            logger.error(ex.getMessage(), ex);
            return new ResponseEntity(ex, HttpStatus.OK);
            //return Response.status(BAD_REQUEST).entity(msg).build();
        }
    }
}