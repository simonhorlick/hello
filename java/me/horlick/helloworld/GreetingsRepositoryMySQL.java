package me.horlick.helloworld;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.HostAndPort;
import com.youtube.vitess.client.Context;
import com.youtube.vitess.client.RpcClient;
import com.youtube.vitess.client.VTGateBlockingConn;
import com.youtube.vitess.client.VTGateBlockingTx;
import com.youtube.vitess.client.cursor.Cursor;
import com.youtube.vitess.client.cursor.Row;
import com.youtube.vitess.client.grpc.GrpcClientFactory;
import com.youtube.vitess.proto.Topodata;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Map;

class GreetingsRepositoryMySQL implements GreetingsRepository {

  private final Context ctx;
  private final VTGateBlockingConn conn;

  private GreetingsRepositoryMySQL(Context ctx, VTGateBlockingConn conn) {
    this.ctx = ctx;
    this.conn = conn;
  }

  public static GreetingsRepositoryMySQL create(HostAndPort hostAndPort) {
    InetSocketAddress addr =
        new InetSocketAddress(hostAndPort.getHostText(), hostAndPort.getPort());
    Context ctx = Context.getDefault();
    //.withDeadlineAfter(Duration.millis(10 * 1000));

    RpcClient client = new GrpcClientFactory().create(ctx, addr);
    VTGateBlockingConn conn = new VTGateBlockingConn(client);

    return new GreetingsRepositoryMySQL(ctx, conn);
  }

  @Override
  public void insertGreet(String name, long timeNs) {
    // Insert this greet into the database.
    Map<String, Object> values =
        new ImmutableMap.Builder<String, Object>().put("name", name).put("time_ns", timeNs).build();
    try {
      VTGateBlockingTx tx = conn.begin(ctx);
      tx.execute(
          ctx,
          "INSERT INTO greetings (name, time_ns) VALUES (:name, :time_ns)",
          values,
          Topodata.TabletType.MASTER);
      tx.commit(ctx);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public NameHistogram nameHistogram() {
    NameHistogram.Builder nameHistogramBuilder = NameHistogram.newBuilder();

    // Query the number of times each name has been greeted.
    try (Cursor cursor =
        conn.execute(
            ctx,
            "SELECT name, COUNT(1) AS count FROM greetings GROUP BY name",
            null,
            Topodata.TabletType.REPLICA)) {
      Row row;
      while ((row = cursor.next()) != null) {
        byte[] name = row.getBytes("name");
        long count = row.getLong("count");
        nameHistogramBuilder.addEntry(
            HistogramEntry.newBuilder().setName(new String(name)).setCount(count).build());
      }
    } catch (Exception e) {
      // Retry?
      throw new RuntimeException(e);
    }

    return nameHistogramBuilder.build();
  }
}
